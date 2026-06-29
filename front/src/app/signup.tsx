import { useMemo, useState } from 'react';
import { Link, router } from 'expo-router';
import {
  Calendar,
  Check,
  HeartPulse,
  IdCard,
  Lock,
  Mail,
  MapPin,
  Phone,
  User,
} from 'lucide-react-native';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppTextInput } from '@/components/app-text-input';
import { BackButton } from '@/components/back-button';
import { BrandMark } from '@/components/brand';
import { PrimaryButton } from '@/components/primary-button';
import { RoleSelector, type Role } from '@/components/role-selector';
import { ScreenContainer } from '@/components/screen-container';
import { useAuth } from '@/hooks/useAuth';
import { ApiError } from '@/services/api';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';
import type { Address, AssistedPerson, CaregiverProfile, UserBase } from '@/types/auth';

const emailRegex = /\S+@\S+\.\S+/;

function splitList(value: string) {
  return value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);
}

function getSignupFeedback(error: unknown) {
  if (error instanceof ApiError) {
    return error.message;
  }

  return 'Nao foi possivel criar sua conta. Tente novamente.';
}

export default function SignupScreen() {
  const { registerCaregiver, registerResponsible } = useAuth();
  const [role, setRole] = useState<Role>('family');
  const [step, setStep] = useState(0);
  const [acceptedTerms, setAcceptedTerms] = useState(true);

  const [fullName, setFullName] = useState('');
  const [cpf, setCpf] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirmation, setPasswordConfirmation] = useState('');
  const [phone, setPhone] = useState('');
  const [birthDate, setBirthDate] = useState('');
  const [city, setCity] = useState('');
  const [stateUf, setStateUf] = useState('');

  const [relationship, setRelationship] = useState('');
  const [contactPreference, setContactPreference] = useState('');
  const [assistedName, setAssistedName] = useState('');
  const [assistedBirthDate, setAssistedBirthDate] = useState('');
  const [assistedCpf, setAssistedCpf] = useState('');
  const [dependencyLevel, setDependencyLevel] = useState('');
  const [mobility, setMobility] = useState('');
  const [careNeeds, setCareNeeds] = useState('');
  const [allergies, setAllergies] = useState('');
  const [medications, setMedications] = useState('');
  const [foodRestrictions, setFoodRestrictions] = useState('');
  const [notes, setNotes] = useState('');
  const [careAddress, setCareAddress] = useState<Address>({
    cep: '',
    rua: '',
    numero: '',
    complemento: '',
    bairro: '',
    cidade: '',
    estado: '',
    pontoReferencia: '',
  });
  const [emergencyName, setEmergencyName] = useState('');
  const [emergencyPhone, setEmergencyPhone] = useState('');
  const [emergencyRelation, setEmergencyRelation] = useState('');

  const [experience, setExperience] = useState('');
  const [education, setEducation] = useState('');
  const [bio, setBio] = useState('');
  const [availability, setAvailability] = useState('');
  const [careRegion, setCareRegion] = useState('');
  const [careMode, setCareMode] = useState('');
  const [services, setServices] = useState('');

  const [feedback, setFeedback] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const maxStep = role === 'family' ? 3 : 2;
  const title = useMemo(() => {
    if (step === 0) return 'Escolha o tipo de conta';
    if (role === 'family' && step === 1) return 'Dados do responsavel';
    if (role === 'family' && step === 2) return 'Pessoa assistida';
    if (role === 'family') return 'Cuidado e emergencia';
    if (step === 1) return 'Dados pessoais';
    return 'Dados profissionais';
  }, [role, step]);

  function updateCareAddress(field: keyof Address, value: string) {
    setCareAddress((current) => ({ ...current, [field]: value }));
  }

  function validatePersonalData() {
    if (!fullName.trim()) return 'Informe seu nome completo.';
    if (!cpf.trim()) return 'Informe seu CPF.';
    if (!email.trim()) return 'Informe seu e-mail.';
    if (!emailRegex.test(email.trim())) return 'Informe um e-mail valido.';
    if (!password) return 'Informe uma senha.';
    if (passwordConfirmation && passwordConfirmation !== password) return 'A confirmacao de senha deve ser igual a senha.';
    if (!phone.trim()) return 'Informe seu telefone.';
    if (!birthDate.trim()) return 'Informe sua data de nascimento.';
    if (role === 'family' && !relationship.trim()) return 'Informe o vinculo com a pessoa assistida.';
    if (role === 'caregiver' && (!city.trim() || !stateUf.trim())) return 'Informe cidade e estado.';
    return null;
  }

  function validateResponsibleAssistedPerson() {
    if (!assistedName.trim()) return 'Informe o nome da pessoa assistida.';
    if (!assistedBirthDate.trim()) return 'Informe a data de nascimento da pessoa assistida.';
    if (!dependencyLevel.trim()) return 'Informe o grau de dependencia.';
    if (!mobility.trim()) return 'Informe a mobilidade.';
    if (splitList(careNeeds).length === 0) return 'Informe ao menos uma necessidade de cuidado.';
    return null;
  }

  function validateResponsibleCareDetails() {
    if (!careAddress.cep.trim()) return 'Informe o CEP do cuidado.';
    if (!careAddress.rua.trim()) return 'Informe a rua do cuidado.';
    if (!careAddress.numero.trim()) return 'Informe o numero do cuidado.';
    if (!careAddress.bairro.trim()) return 'Informe o bairro do cuidado.';
    if (!careAddress.cidade.trim()) return 'Informe a cidade do cuidado.';
    if (!careAddress.estado.trim()) return 'Informe o estado do cuidado.';
    if (!emergencyName.trim() || !emergencyPhone.trim() || !emergencyRelation.trim()) {
      return 'Informe nome, telefone e vinculo do contato de emergencia.';
    }
    return null;
  }

  function validateCaregiverProfessionalData() {
    if (!experience.trim()) return 'Informe sua experiencia.';
    if (splitList(availability).length === 0) return 'Informe sua disponibilidade.';
    if (!careRegion.trim()) return 'Informe sua regiao de atendimento.';
    if (!careMode.trim()) return 'Informe a modalidade de atendimento.';
    if (splitList(services).length === 0) return 'Informe ao menos um servico oferecido.';
    return null;
  }

  function validateCurrentStep() {
    if (step === 0) return null;
    if (step === 1) return validatePersonalData();
    if (role === 'family' && step === 2) return validateResponsibleAssistedPerson();
    if (role === 'family' && step === 3) return validateResponsibleCareDetails();
    if (role === 'caregiver' && step === 2) return validateCaregiverProfessionalData();
    return null;
  }

  function buildUser(tipoUsuario: UserBase['tipoUsuario']): UserBase {
    return {
      nome: fullName.trim(),
      cpf: cpf.trim(),
      email: email.trim(),
      telefone: phone.trim(),
      dataNascimento: birthDate.trim(),
      tipoUsuario,
      status: 'ACTIVE',
    };
  }

  function buildAssistedPerson(): AssistedPerson {
    return {
      nome: assistedName.trim(),
      dataNascimento: assistedBirthDate.trim(),
      cpf: assistedCpf.trim() || undefined,
      grauDependencia: dependencyLevel.trim(),
      mobilidade: mobility.trim(),
      necessidadesCuidado: splitList(careNeeds),
      alergias: allergies.trim() || undefined,
      medicamentos: medications.trim() || undefined,
      restricoesAlimentares: foodRestrictions.trim() || undefined,
      observacoes: notes.trim() || undefined,
      enderecoCuidado: careAddress,
      contatoEmergencia: {
        nome: emergencyName.trim(),
        telefone: emergencyPhone.trim(),
        vinculo: emergencyRelation.trim(),
      },
    };
  }

  function buildCaregiverProfile(): CaregiverProfile {
    return {
      experiencia: experience.trim(),
      formacao: education.trim() || undefined,
      biografia: bio.trim() || undefined,
      disponibilidade: splitList(availability),
      regiaoAtendimento: careRegion.trim(),
      modalidadeAtendimento: careMode.trim(),
      servicosOferecidos: splitList(services),
    };
  }

  async function handleNext() {
    setFeedback(null);

    const validationFeedback = validateCurrentStep();
    if (validationFeedback) {
      setFeedback(validationFeedback);
      return;
    }

    if (step < maxStep) {
      setStep((current) => current + 1);
      return;
    }

    if (!acceptedTerms) {
      setFeedback('Aceite os Termos e a Politica de Privacidade.');
      return;
    }

    try {
      setIsSubmitting(true);
      if (role === 'family') {
        await registerResponsible({
          user: buildUser('RESPONSAVEL'),
          senha: password,
          responsibleProfile: {
            parentescoPadrao: relationship.trim(),
            contatoPreferencial: contactPreference.trim() || undefined,
          },
          assistedPersons: [buildAssistedPerson()],
          acceptedTerms,
        });
      } else {
        await registerCaregiver({
          user: buildUser('CUIDADOR'),
          senha: password,
          caregiverProfile: buildCaregiverProfile(),
          acceptedTerms,
        });
      }
      router.replace('/profile');
    } catch (error) {
      setFeedback(getSignupFeedback(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <View style={styles.topRow}>
        <BackButton />
        <BrandMark />
      </View>

      <View style={styles.heading}>
        <Text style={styles.title}>Criar sua conta</Text>
        <Text style={styles.subtitle}>{title}</Text>
      </View>

      {step === 0 ? <RoleSelector value={role} onChange={setRole} /> : null}

      <View style={styles.form}>
        {step === 1 ? (
          <>
            <AppTextInput label="Nome completo" icon={User} placeholder="Maria da Silva" value={fullName} onChangeText={setFullName} autoCapitalize="words" />
            <AppTextInput label="CPF" icon={IdCard} placeholder="000.000.000-00" value={cpf} onChangeText={setCpf} keyboardType="number-pad" />
            <AppTextInput label="E-mail" icon={Mail} placeholder="seu@email.com" value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" />
            <AppTextInput label="Telefone" icon={Phone} placeholder="(00) 00000-0000" value={phone} onChangeText={setPhone} keyboardType="phone-pad" />
            <AppTextInput label="Data de nascimento" icon={Calendar} placeholder="dd/mm/aaaa" value={birthDate} onChangeText={setBirthDate} keyboardType="number-pad" />
            <AppTextInput label="Senha" icon={Lock} placeholder="********" value={password} onChangeText={setPassword} secureTextEntry />
            <AppTextInput label="Confirmar senha" icon={Lock} placeholder="********" value={passwordConfirmation} onChangeText={setPasswordConfirmation} secureTextEntry />
            {role === 'family' ? (
              <>
                <AppTextInput label="Parentesco ou vinculo" icon={HeartPulse} placeholder="Filha, neto, responsavel legal..." value={relationship} onChangeText={setRelationship} />
                <AppTextInput label="Preferencia de contato" icon={Phone} placeholder="WhatsApp, ligacao, e-mail..." value={contactPreference} onChangeText={setContactPreference} />
              </>
            ) : (
              <>
                <AppTextInput label="Cidade" icon={MapPin} placeholder="Sua cidade" value={city} onChangeText={setCity} />
                <AppTextInput label="Estado" icon={MapPin} placeholder="UF" value={stateUf} onChangeText={setStateUf} autoCapitalize="characters" />
              </>
            )}
          </>
        ) : null}

        {role === 'family' && step === 2 ? (
          <>
            <AppTextInput label="Nome da pessoa assistida" icon={User} placeholder="Nome completo" value={assistedName} onChangeText={setAssistedName} />
            <AppTextInput label="Data de nascimento" icon={Calendar} placeholder="dd/mm/aaaa" value={assistedBirthDate} onChangeText={setAssistedBirthDate} keyboardType="number-pad" />
            <AppTextInput label="CPF da pessoa assistida" icon={IdCard} placeholder="Opcional" value={assistedCpf} onChangeText={setAssistedCpf} keyboardType="number-pad" />
            <AppTextInput label="Grau de dependencia" icon={HeartPulse} placeholder="Baixo, moderado, alto..." value={dependencyLevel} onChangeText={setDependencyLevel} />
            <AppTextInput label="Mobilidade" icon={HeartPulse} placeholder="Independente, usa bengala..." value={mobility} onChangeText={setMobility} />
            <AppTextInput label="Necessidades de cuidado" icon={HeartPulse} placeholder="Separadas por virgula" value={careNeeds} onChangeText={setCareNeeds} />
            <AppTextInput label="Alergias" icon={HeartPulse} placeholder="Opcional" value={allergies} onChangeText={setAllergies} />
            <AppTextInput label="Medicamentos" icon={HeartPulse} placeholder="Opcional" value={medications} onChangeText={setMedications} />
            <AppTextInput label="Restricoes alimentares" icon={HeartPulse} placeholder="Opcional" value={foodRestrictions} onChangeText={setFoodRestrictions} />
            <AppTextInput label="Observacoes importantes" icon={HeartPulse} placeholder="Opcional" value={notes} onChangeText={setNotes} />
          </>
        ) : null}

        {role === 'family' && step === 3 ? (
          <>
            <AppTextInput label="CEP" icon={MapPin} placeholder="00000-000" value={careAddress.cep} onChangeText={(value) => updateCareAddress('cep', value)} keyboardType="number-pad" />
            <AppTextInput label="Rua" icon={MapPin} placeholder="Rua do cuidado" value={careAddress.rua} onChangeText={(value) => updateCareAddress('rua', value)} />
            <AppTextInput label="Numero" icon={MapPin} placeholder="123" value={careAddress.numero} onChangeText={(value) => updateCareAddress('numero', value)} />
            <AppTextInput label="Complemento" icon={MapPin} placeholder="Opcional" value={careAddress.complemento} onChangeText={(value) => updateCareAddress('complemento', value)} />
            <AppTextInput label="Bairro" icon={MapPin} placeholder="Bairro" value={careAddress.bairro} onChangeText={(value) => updateCareAddress('bairro', value)} />
            <AppTextInput label="Cidade" icon={MapPin} placeholder="Cidade" value={careAddress.cidade} onChangeText={(value) => updateCareAddress('cidade', value)} />
            <AppTextInput label="Estado" icon={MapPin} placeholder="UF" value={careAddress.estado} onChangeText={(value) => updateCareAddress('estado', value)} autoCapitalize="characters" />
            <AppTextInput label="Ponto de referencia" icon={MapPin} placeholder="Opcional" value={careAddress.pontoReferencia} onChangeText={(value) => updateCareAddress('pontoReferencia', value)} />
            <AppTextInput label="Contato de emergencia" icon={User} placeholder="Nome completo" value={emergencyName} onChangeText={setEmergencyName} />
            <AppTextInput label="Telefone de emergencia" icon={Phone} placeholder="(00) 00000-0000" value={emergencyPhone} onChangeText={setEmergencyPhone} keyboardType="phone-pad" />
            <AppTextInput label="Vinculo do contato" icon={HeartPulse} placeholder="Irma, vizinho, amigo..." value={emergencyRelation} onChangeText={setEmergencyRelation} />
          </>
        ) : null}

        {role === 'caregiver' && step === 2 ? (
          <>
            <AppTextInput label="Experiencia" icon={HeartPulse} placeholder="Resumo da experiencia" value={experience} onChangeText={setExperience} />
            <AppTextInput label="Formacao" icon={HeartPulse} placeholder="Opcional" value={education} onChangeText={setEducation} />
            <AppTextInput label="Biografia profissional" icon={HeartPulse} placeholder="Opcional" value={bio} onChangeText={setBio} />
            <AppTextInput label="Disponibilidade" icon={Calendar} placeholder="Dias e horarios separados por virgula" value={availability} onChangeText={setAvailability} />
            <AppTextInput label="Regiao de atendimento" icon={MapPin} placeholder="Bairros, cidades ou raio de atendimento" value={careRegion} onChangeText={setCareRegion} />
            <AppTextInput label="Modalidade de atendimento" icon={HeartPulse} placeholder="Domiciliar, acompanhamento, plantao..." value={careMode} onChangeText={setCareMode} />
            <AppTextInput label="Servicos oferecidos" icon={HeartPulse} placeholder="Separados por virgula" value={services} onChangeText={setServices} />
          </>
        ) : null}

        {step === maxStep ? (
          <Pressable
            accessibilityRole="checkbox"
            accessibilityState={{ checked: acceptedTerms }}
            onPress={() => setAcceptedTerms((value) => !value)}
            style={({ pressed }) => [styles.termsRow, pressed && styles.pressed]}
          >
            <View style={[styles.checkbox, acceptedTerms && styles.checkboxChecked]}>
              {acceptedTerms ? <Check color={colors.primaryForeground} size={14} strokeWidth={3} /> : null}
            </View>
            <Text style={styles.termsText}>
              Concordo com os <Text style={styles.termsLink}>Termos</Text> e a{' '}
              <Text style={styles.termsLink}>Politica de Privacidade</Text>.
            </Text>
          </Pressable>
        ) : null}

        {feedback ? <Text style={styles.errorText}>{feedback}</Text> : null}

        <View style={styles.actions}>
          {step > 0 ? (
            <PrimaryButton label="Voltar" variant="secondary" onPress={() => setStep((current) => current - 1)} disabled={isSubmitting} />
          ) : null}
          <PrimaryButton
            label={step < maxStep ? 'Continuar' : isSubmitting ? 'Criando conta...' : 'Criar conta'}
            onPress={handleNext}
            disabled={isSubmitting}
          />
        </View>
      </View>

      <Text style={styles.footerText}>
        Ja tem conta? <Link href="/login" style={styles.footerLink}>Entrar</Link>
      </Text>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.xxl,
  },
  topRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: spacing.xl,
  },
  heading: {
    marginBottom: spacing.xl,
  },
  title: {
    fontFamily: fontFamily.extraBold,
    fontSize: 26,
    lineHeight: 32,
    color: colors.foreground,
  },
  subtitle: {
    marginTop: spacing.xs,
    marginBottom: spacing.xs,
    fontFamily: fontFamily.regular,
    fontSize: 13.5,
    lineHeight: 20,
    color: colors.mutedForeground,
  },
  form: {
    marginTop: spacing.xl,
    gap: spacing.md,
  },
  actions: {
    gap: spacing.md,
  },
  termsRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: spacing.sm,
    paddingTop: spacing.xs,
  },
  pressed: {
    opacity: 0.78,
  },
  checkbox: {
    width: 20,
    height: 20,
    borderRadius: radii.sm,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 1,
  },
  checkboxChecked: {
    borderColor: colors.primary,
    backgroundColor: colors.primary,
  },
  termsText: {
    flex: 1,
    fontFamily: fontFamily.regular,
    fontSize: 11.5,
    lineHeight: 17,
    color: colors.mutedForeground,
  },
  termsLink: {
    fontFamily: fontFamily.semiBold,
    color: colors.primary,
  },
  errorText: {
    fontFamily: fontFamily.medium,
    fontSize: 12,
    lineHeight: 18,
    color: colors.destructive,
  },
  footerText: {
    paddingTop: spacing.xl,
    textAlign: 'center',
    fontFamily: fontFamily.regular,
    fontSize: 13,
    color: colors.mutedForeground,
  },
  footerLink: {
    fontFamily: fontFamily.semiBold,
    color: colors.primary,
  },
});
