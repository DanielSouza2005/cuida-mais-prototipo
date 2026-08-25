package br.com.cuidaplus.api.attendance_report;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AttendanceReportEmailEventListener {
  private final AttendanceReportEmailWorker worker;

  public AttendanceReportEmailEventListener(AttendanceReportEmailWorker worker) {
    this.worker = worker;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onEmailRequested(AttendanceReportEmailRequested event) {
    worker.deliver(event.reportId());
  }
}
