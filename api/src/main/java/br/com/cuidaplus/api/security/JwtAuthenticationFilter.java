package br.com.cuidaplus.api.security;

import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.profile.CaregiverProfileRepository;
import br.com.cuidaplus.api.profile.ResponsibleProfileRepository;
import br.com.cuidaplus.api.user.AccountStatus;
import br.com.cuidaplus.api.user.User;
import br.com.cuidaplus.api.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final TokenService tokenService;
  private final UserRepository userRepository;
  private final CaregiverProfileRepository caregiverProfiles;
  private final ResponsibleProfileRepository responsibleProfiles;
  private final ObjectMapper objectMapper;

  public JwtAuthenticationFilter(TokenService tokenService, UserRepository userRepository, CaregiverProfileRepository caregiverProfiles,
    ResponsibleProfileRepository responsibleProfiles, ObjectMapper objectMapper) {
    this.tokenService = tokenService;
    this.userRepository = userRepository;
    this.caregiverProfiles = caregiverProfiles;
    this.responsibleProfiles = responsibleProfiles;
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (header != null && header.startsWith("Bearer ")) {
      try {
        UUID userId = tokenService.validateAndGetUserId(header.substring(7));
        User user = userRepository.findById(userId)
          .orElseThrow(() -> new BusinessException("Usuário não encontrado.", HttpStatus.UNAUTHORIZED, "INVALID_SESSION"));
        validateAccess(user);
        UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getUserType().name())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (BusinessException exception) {
        response.setStatus(exception.getStatus().value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), java.util.Map.of(
          "status", exception.getStatus().value(),
          "code", exception.getCode() == null ? "INVALID_SESSION" : exception.getCode(),
          "message", exception.getMessage()
        ));
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  private void validateAccess(User user) {
    if (user.getAccountStatus() != AccountStatus.ATIVO) {
      throw new BusinessException("Sua conta está bloqueada. Entre em contato com o suporte para mais informações.", HttpStatus.FORBIDDEN, "ACCOUNT_BLOCKED");
    }
    if (user.isResponsible()) {
      var profile = responsibleProfiles.findByUser(user)
        .orElseThrow(() -> new BusinessException("Perfil de responsável não encontrado.", HttpStatus.FORBIDDEN, "RESPONSIBLE_PROFILE_MISSING"));
      switch (profile.getSituacaoAprovacao()) {
        case PENDENTE -> throw new BusinessException("Seu cadastro de responsável ainda está em análise.", HttpStatus.FORBIDDEN, "RESPONSIBLE_PENDING_APPROVAL");
        case REPROVADO -> throw new BusinessException("Seu cadastro de responsável foi reprovado. Verifique seu e-mail para mais informações.", HttpStatus.FORBIDDEN, "RESPONSIBLE_REJECTED");
        case BLOQUEADO -> throw new BusinessException("Seu perfil de responsável está bloqueado.", HttpStatus.FORBIDDEN, "RESPONSIBLE_BLOCKED");
        case APROVADO -> { }
      }
      return;
    }
    if (!user.isCaregiver()) return;
    var profile = caregiverProfiles.findByUser(user)
      .orElseThrow(() -> new BusinessException("Perfil de cuidador não encontrado.", HttpStatus.FORBIDDEN, "CAREGIVER_PROFILE_MISSING"));
    switch (profile.getSituacaoAprovacao()) {
      case PENDENTE -> throw new BusinessException("Seu cadastro de cuidador ainda está em análise.", HttpStatus.FORBIDDEN, "CAREGIVER_PENDING_APPROVAL");
      case REPROVADO -> throw new BusinessException("Seu cadastro de cuidador foi reprovado. Verifique seu e-mail para mais informações.", HttpStatus.FORBIDDEN, "CAREGIVER_REJECTED");
      case BLOQUEADO -> throw new BusinessException("Seu perfil de cuidador está bloqueado.", HttpStatus.FORBIDDEN, "CAREGIVER_BLOCKED");
      case APROVADO -> { }
    }
  }
}
