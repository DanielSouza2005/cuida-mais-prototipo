package br.com.cuidaplus.api.service_request;

import br.com.cuidaplus.api.profile.DiaSemana;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalTime;

@Embeddable
public class ServiceRequestScheduleDay {
  @Enumerated(EnumType.STRING) @Column(name = "weekday", length = 20) private DiaSemana weekday;
  @Column(name = "start_time") private LocalTime startTime;
  @Column(name = "end_time") private LocalTime endTime;
  public DiaSemana getWeekday() { return weekday; }
  public void setWeekday(DiaSemana weekday) { this.weekday = weekday; }
  public LocalTime getStartTime() { return startTime; }
  public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
  public LocalTime getEndTime() { return endTime; }
  public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}
