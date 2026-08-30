import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public final class FieldRemovalAudit {
  public static void main(String[] args) throws Exception {
    try (Connection connection = DriverManager.getConnection(
        env("DATABASE_URL"), env("DATABASE_USERNAME"), env("DATABASE_PASSWORD"))) {
      connection.setReadOnly(true);
      print(connection, "flyway_latest", "select version, description from flyway_schema_history where success order by installed_rank desc limit 1");
      print(connection, "caregiver_legacy_formation", """
          select count(*) total,
                 count(*) filter (where formacao is not null and btrim(formacao) <> '') preenchidos,
                 count(*) filter (where formacao is not null and btrim(formacao) <> '' and not exists (
                   select 1 from cuidadores_formacoes cf
                   where cf.perfil_cuidador_id = c.id and cf.formacao = c.formacao
                 )) sem_equivalente
          from cuidadores c
          """);
      print(connection, "caregiver_formations", "select count(*) total, count(distinct perfil_cuidador_id) cuidadores from cuidadores_formacoes");
      print(connection, "caregiver_coordinates", """
          select count(*) total,
                 count(*) filter (where latitude is not null or longitude is not null) algum_valor,
                 count(*) filter (where latitude is not null and longitude is not null) pares_completos,
                 count(*) filter (where (latitude is null) <> (longitude is null)) pares_incompletos
          from cuidadores
          """);
      print(connection, "assisted_coordinates", """
          select count(*) total,
                 count(*) filter (where latitude is not null or longitude is not null) algum_valor,
                 count(*) filter (where latitude is not null and longitude is not null) pares_completos,
                 count(*) filter (where (latitude is null) <> (longitude is null)) pares_incompletos
          from pessoas_assistidas
          """);
      print(connection, "task_timezones", "select fuso_horario, count(*) quantidade from tarefas_cuidado group by fuso_horario order by quantidade desc, fuso_horario");
      print(connection, "activity_timezones", "select fuso_horario, count(*) quantidade from registros_diario_cuidado group by fuso_horario order by quantidade desc, fuso_horario");
      print(connection, "occurrence_timezones", "select fuso_horario, count(*) quantidade from ocorrencias_cuidado group by fuso_horario order by quantidade desc, fuso_horario");
      print(connection, "dependent_constraints", """
          select rel.relname tabela, att.attname coluna, con.conname objeto, 'constraint' tipo
          from pg_constraint con
          join pg_class rel on rel.oid=con.conrelid
          join pg_namespace ns on ns.oid=rel.relnamespace
          join pg_attribute att on att.attrelid=rel.oid and att.attnum=any(con.conkey)
          where ns.nspname='public' and (rel.relname, att.attname) in (
            ('cuidadores','formacao'),('cuidadores','latitude'),('cuidadores','longitude'),
            ('pessoas_assistidas','latitude'),('pessoas_assistidas','longitude'),
            ('tarefas_cuidado','fuso_horario'),('registros_diario_cuidado','fuso_horario'),
            ('ocorrencias_cuidado','fuso_horario'))
          union all
          select rel.relname, att.attname, idx.relname, 'index'
          from pg_index ix
          join pg_class rel on rel.oid=ix.indrelid
          join pg_namespace ns on ns.oid=rel.relnamespace
          join pg_class idx on idx.oid=ix.indexrelid
          join pg_attribute att on att.attrelid=rel.oid and att.attnum=any(ix.indkey)
          where ns.nspname='public' and (rel.relname, att.attname) in (
            ('cuidadores','formacao'),('cuidadores','latitude'),('cuidadores','longitude'),
            ('pessoas_assistidas','latitude'),('pessoas_assistidas','longitude'),
            ('tarefas_cuidado','fuso_horario'),('registros_diario_cuidado','fuso_horario'),
            ('ocorrencias_cuidado','fuso_horario'))
          order by tabela, coluna, tipo, objeto
          """);
    }
  }

  private static void print(Connection connection, String title, String sql) throws Exception {
    System.out.println("[" + title + "]");
    try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery(sql)) {
      int columns = rows.getMetaData().getColumnCount();
      while (rows.next()) {
        for (int index = 1; index <= columns; index++) {
          if (index > 1) System.out.print(" | ");
          System.out.print(rows.getMetaData().getColumnLabel(index) + "=" + rows.getString(index));
        }
        System.out.println();
      }
    }
  }

  private static String env(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) throw new IllegalStateException(name + " is required");
    return value;
  }
}
