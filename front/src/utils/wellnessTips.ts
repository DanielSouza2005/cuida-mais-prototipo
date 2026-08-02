const wellnessTips = [
  'Faça uma pausa breve para respirar fundo e relaxar os ombros.',
  'Mantenha uma garrafa de água por perto e hidrate-se ao longo do dia.',
  'Reconheça o que você conseguiu fazer hoje, mesmo que pareça pequeno.',
  'Reserve alguns minutos para alongar braços, pescoço e costas.',
  'Peça ajuda quando precisar. Cuidar também é uma responsabilidade compartilhada.',
  'Uma rotina possível é melhor do que uma rotina perfeita.',
  'Faça uma refeição com calma e atenção sempre que puder.',
  'Organize primeiro o cuidado mais urgente e avance um passo de cada vez.',
  'Abra uma janela ou caminhe um pouco para renovar o ambiente e a energia.',
  'Fale consigo com a mesma gentileza que oferece a quem você cuida.',
  'Pequenos intervalos ajudam a manter a atenção e a qualidade do cuidado.',
  'Observe sua postura durante as atividades e ajuste-a quando necessário.',
  'Compartilhe uma preocupação com alguém de confiança.',
  'Celebre os momentos tranquilos e as pequenas conquistas do dia.',
  'Descanse quando houver oportunidade; recuperar energia também faz parte do cuidado.',
  'Prepare com antecedência apenas o necessário para o próximo cuidado.',
  'Ouça uma música que traga calma durante uma pausa.',
  'Respire devagar por um minuto antes de uma atividade que exige atenção.',
  'Evite cobrar de si respostas para tudo. Presença e escuta já fazem diferença.',
  'Inclua um momento agradável na sua rotina, mesmo que dure poucos minutos.',
  'Revise os horários do dia sem esquecer de reservar tempo para você.',
  'Mantenha por perto os contatos importantes para se sentir mais seguro.',
  'Observe sinais de cansaço e reduza o ritmo quando for possível.',
  'Uma conversa acolhedora pode tornar o cuidado mais leve para todos.',
  'Priorize uma boa noite de sono sempre que a rotina permitir.',
  'Anote informações importantes para não depender apenas da memória.',
  'Movimente o corpo com suavidade depois de permanecer muito tempo na mesma posição.',
  'Aceite que alguns dias serão mais difíceis e ajuste suas expectativas.',
  'Agradeça a si mesmo pelo cuidado e pela atenção dedicados hoje.',
  'Termine o dia lembrando de algo que trouxe conforto ou alegria.',
] as const;

export function getWellnessTip(date = new Date()) {
  const currentDay = Date.UTC(date.getFullYear(), date.getMonth(), date.getDate());
  const firstDay = Date.UTC(date.getFullYear(), 0, 1);
  const dayOfYear = Math.floor((currentDay - firstDay) / 86_400_000);
  return wellnessTips[dayOfYear % wellnessTips.length];
}

export { wellnessTips };
