package br.com.cuidaplus.api.service_request.dto;
import jakarta.validation.constraints.Size;
public record RejectServiceRequestRequest(@Size(max=1000) String reason){}
