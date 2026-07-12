import { useEffect, useMemo, useRef, useState } from 'react';
import { router } from 'expo-router';
import * as ImagePicker from 'expo-image-picker';
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
import { Alert, Pressable, StyleSheet, Text, View } from 'react-native';

import { AppTextInput } from '@/components/app-text-input';
import { BackButton } from '@/components/back-button';
import { BrandMark } from '@/components/brand';
import { DatePickerField } from '@/components/date-picker-field';
import { OptionGroup } from '@/components/option-group';
import { PrimaryButton } from '@/components/primary-button';
import { ProfileAvatar } from '@/components/profile-avatar';
import { RoleSelector, type Role } from '@/components/role-selector';
import { ScreenContainer } from '@/components/screen-container';
import {
  allergyOptions,
  caregiverEducationOptions,
  caregiverExperienceRangeOptions,
  caregiverServiceOptions,
  careModalityOptions,
  contactPreferenceOptions,
  dayPeriodOptions,
  dependencyLevelOptions,
  foodRestrictionOptions,
  mobilityOptions,
  relationshipOptions,
  weekDayOptions,
  type Allergy,
  type CaregiverEducation,
  type CaregiverExperienceRange,
  type CaregiverService,
  type CareModality,
  type ContactPreference,
  type DayPeriod,
  type DependencyLevel,
  type FoodRestriction,
  type Mobility,
  type Relationship,
  type WeekDay,
} from '@/constants/enums';
import { useAuth } from '@/hooks/useAuth';
import { useBlockNavigationWhenBusy } from '@/hooks/useBlockNavigationWhenBusy';
import { ApiError } from '@/services/api';
import { CepError, getAddressByCep } from '@/services/cepService';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';
import type { Address, AssistedPerson, CaregiverProfile, SelectedProfilePhoto, UserBase } from '@/types/auth';
import { MAX_PROFILE_PHOTO_SIZE, toSelectedProfilePhoto } from '@/utils/profilePhoto';
import {
  formatCep,
  formatCpf,
  formatPhone,
  isValidEmailFormat,
  unformatCep,
  unformatCpf,
  unformatPhone,
} from '@/utils/masks';

const emptyAddress: Address = {
  cep: '',
  rua: '',
  numero: '',
  complemento: '',
  bairro: '',
  cidade: '',
  estado: '',
  pontoReferencia: '',
};

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

  return 'Não foi possível criar sua conta. Tente novamente.';
}

function needsDetail(options: string[]) {
  return options.some((option) => option !== 'NAO_POSSUI' && option !== 'NAO_SEI_INFORMAR');
}

function normalizeExclusiveHealthOptions<T extends string>(values: T[]) {
  const lastValue = values[values.length - 1];
  const exclusiveOptions = ['NAO_POSSUI', 'NAO_SEI_INFORMAR'];

  if (!lastValue) return values;
  if (exclusiveOptions.includes(lastValue)) return [lastValue];
  return values.filter((value) => !exclusiveOptions.includes(value));
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

  const [relationship, setRelationship] = useState<Relationship | null>(null);
  const [relationshipCustom, setRelationshipCustom] = useState('');
  const [contactPreference, setContactPreference] = useState<ContactPreference | null>(null);
  const [assistedName, setAssistedName] = useState('');
  const [assistedBirthDate, setAssistedBirthDate] = useState('');
  const [assistedCpf, setAssistedCpf] = useState('');
  const [dependencyLevel, setDependencyLevel] = useState<DependencyLevel | null>(null);
  const [mobility, setMobility] = useState<Mobility | null>(null);
  const [mobilityCustom, setMobilityCustom] = useState('');
  const [careNeeds, setCareNeeds] = useState('');
  const [allergies, setAllergies] = useState<Allergy[]>([]);
  const [allergyDetails, setAllergyDetails] = useState('');
  const [medications, setMedications] = useState('');
  const [foodRestrictions, setFoodRestrictions] = useState<FoodRestriction[]>([]);
  const [foodRestrictionDetails, setFoodRestrictionDetails] = useState('');
  const [notes, setNotes] = useState('');
  const [careAddress, setCareAddress] = useState<Address>(emptyAddress);
  const [careCepFeedback, setCareCepFeedback] = useState<string | null>(null);
  const [careCepFeedbackKind, setCareCepFeedbackKind] = useState<'info' | 'success' | 'error'>('info');
  const [isFetchingCareCep, setIsFetchingCareCep] = useState(false);
  const [emergencyName, setEmergencyName] = useState('');
  const [emergencyPhone, setEmergencyPhone] = useState('');
  const [emergencyRelation, setEmergencyRelation] = useState('');
  const [useResponsibleAsEmergencyContact, setUseResponsibleAsEmergencyContact] = useState(false);

  const [caregiverAddress, setCaregiverAddress] = useState<Address>(emptyAddress);
  const [cepFeedback, setCepFeedback] = useState<string | null>(null);
  const [cepFeedbackKind, setCepFeedbackKind] = useState<'info' | 'success' | 'error'>('info');
  const [isFetchingCep, setIsFetchingCep] = useState(false);
  const [experienceRange, setExperienceRange] = useState<CaregiverExperienceRange | null>(null);
  const [education, setEducation] = useState<CaregiverEducation[]>([]);
  const [educationCustom, setEducationCustom] = useState('');
  const [bio, setBio] = useState('');
  const [weekDays, setWeekDays] = useState<WeekDay[]>([]);
  const [dayPeriods, setDayPeriods] = useState<DayPeriod[]>([]);
  const [availabilityStart, setAvailabilityStart] = useState('');
  const [availabilityEnd, setAvailabilityEnd] = useState('');
  const [availabilityNote, setAvailabilityNote] = useState('');
  const [careModes, setCareModes] = useState<CareModality[]>([]);
  const [careModeCustom, setCareModeCustom] = useState('');
  const [services, setServices] = useState<CaregiverService[]>([]);
  const [serviceCustom, setServiceCustom] = useState('');
  const [profilePhoto, setProfilePhoto] = useState<SelectedProfilePhoto | null>(null);

  const [feedback, setFeedback] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const today = useMemo(() => new Date(), []);
  const caregiverCepRequestRef = useRef('');
  const careCepRequestRef = useRef('');

  const maxStep = role === 'family' ? 4 : 4;
  const showAllergyDetails = needsDetail(allergies);
  const showFoodRestrictionDetails = needsDetail(foodRestrictions);
  const usesCustomAvailability = dayPeriods.includes('HORARIO_PERSONALIZADO');
  const screenBusy = isSubmitting || isFetchingCep || isFetchingCareCep;
  const formDisabled = screenBusy;
  const careAddressDisabled = screenBusy;
  const caregiverAddressDisabled = screenBusy;
  useBlockNavigationWhenBusy(screenBusy);

  function handlePickedAsset(asset: ImagePicker.ImagePickerAsset) {
    if (asset.fileSize && asset.fileSize > MAX_PROFILE_PHOTO_SIZE) {
      Alert.alert('Foto muito grande', 'Escolha uma foto de até 5 MB.');
      return;
    }
    setProfilePhoto(toSelectedProfilePhoto(asset));
  }

  async function pickProfilePhoto(source: 'camera' | 'library') {
    const permission = source === 'camera'
      ? await ImagePicker.requestCameraPermissionsAsync()
      : await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permission.granted) {
      Alert.alert('Permissão negada', 'Permissão negada. Você pode continuar o cadastro sem foto.');
      return;
    }
    const result = source === 'camera'
      ? await ImagePicker.launchCameraAsync({ mediaTypes: ['images'], allowsEditing: true, aspect: [1, 1], quality: 0.8, cameraType: ImagePicker.CameraType.front })
      : await ImagePicker.launchImageLibraryAsync({ mediaTypes: ['images'], allowsEditing: true, aspect: [1, 1], quality: 0.8 });
    if (!result.canceled && result.assets[0]) handlePickedAsset(result.assets[0]);
  }

  function showPhotoOptions() {
    Alert.alert(profilePhoto ? 'Trocar foto' : 'Adicionar foto', undefined, [
      { text: 'Tirar foto', onPress: () => void pickProfilePhoto('camera') },
      { text: 'Escolher da galeria', onPress: () => void pickProfilePhoto('library') },
      { text: 'Cancelar', style: 'cancel' },
    ]);
  }

  const title = useMemo(() => {
    if (step === 0) return 'Escolha o tipo de conta';
    if (role === 'family' && step === 1) return 'Dados da conta';
    if (role === 'family' && step === 2) return 'Vínculo e contato';
    if (role === 'family' && step === 3) return 'Pessoa assistida';
    if (role === 'family') return 'Cuidado e emergência';
    if (step === 1) return 'Dados pessoais';
    if (step === 2) return 'Endereço do cuidador';
    if (step === 3) return 'Dados profissionais';
    return 'Disponibilidade e serviços';
  }, [role, step]);

  useEffect(() => {
    const cleanCep = unformatCep(caregiverAddress.cep);
    if (role !== 'caregiver' || cleanCep.length !== 8) {
      caregiverCepRequestRef.current = '';
      setCepFeedback(null);
      setCepFeedbackKind('info');
      return;
    }

    if (caregiverCepRequestRef.current === cleanCep) return;
    caregiverCepRequestRef.current = cleanCep;

    let active = true;

    async function fetchAddress() {
      setIsFetchingCep(true);
      setCepFeedback('Consultando CEP...');
      setCepFeedbackKind('info');

      try {
        const address = await getAddressByCep(cleanCep);
        if (!active) return;

        setCaregiverAddress((current) => ({
          ...current,
          cep: formatCep(address.cep ?? current.cep),
          rua: address.rua ?? current.rua,
          bairro: address.bairro ?? current.bairro,
          cidade: address.cidade ?? current.cidade,
          estado: address.estado ?? current.estado,
          complemento: current.complemento?.trim() ? current.complemento : address.complemento ?? current.complemento,
        }));
        setCepFeedback('Endereço preenchido pelo CEP. Você pode editar se precisar.');
        setCepFeedbackKind('success');
      } catch (error) {
        if (!active) return;
        setCepFeedback(error instanceof CepError ? error.message : 'Não foi possível consultar o CEP agora.');
        setCepFeedbackKind('error');
      } finally {
        if (active) setIsFetchingCep(false);
      }
    }

    fetchAddress();

    return () => {
      active = false;
    };
  }, [caregiverAddress.cep, role]);

  useEffect(() => {
    const cleanCep = unformatCep(careAddress.cep);
    if (role !== 'family' || cleanCep.length !== 8) {
      careCepRequestRef.current = '';
      setCareCepFeedback(null);
      setCareCepFeedbackKind('info');
      return;
    }

    if (careCepRequestRef.current === cleanCep) return;
    careCepRequestRef.current = cleanCep;

    let active = true;

    async function fetchAddress() {
      setIsFetchingCareCep(true);
      setCareCepFeedback('Consultando CEP...');
      setCareCepFeedbackKind('info');

      try {
        const address = await getAddressByCep(cleanCep);
        if (!active) return;

        setCareAddress((current) => ({
          ...current,
          cep: formatCep(address.cep ?? current.cep),
          rua: address.rua ?? current.rua,
          bairro: address.bairro ?? current.bairro,
          cidade: address.cidade ?? current.cidade,
          estado: address.estado ?? current.estado,
          complemento: current.complemento?.trim() ? current.complemento : address.complemento ?? current.complemento,
        }));
        setCareCepFeedback('Endereço do cuidado preenchido pelo CEP. Você pode editar se precisar.');
        setCareCepFeedbackKind('success');
      } catch (error) {
        if (!active) return;
        setCareCepFeedback(error instanceof CepError ? error.message : 'Não foi possível consultar o CEP agora.');
        setCareCepFeedbackKind('error');
      } finally {
        if (active) setIsFetchingCareCep(false);
      }
    }

    fetchAddress();

    return () => {
      active = false;
    };
  }, [careAddress.cep, role]);

  function updateCareAddress(field: keyof Address, value: string) {
    setCareAddress((current) => ({ ...current, [field]: field === 'cep' ? formatCep(value) : value }));
  }

  function updateCaregiverAddress(field: keyof Address, value: string) {
    setCaregiverAddress((current) => ({ ...current, [field]: field === 'cep' ? formatCep(value) : value }));
  }

  function validatePersonalData() {
    if (!fullName.trim()) return 'Informe seu nome completo.';
    if (!cpf.trim()) return 'Informe seu CPF.';
    if (!email.trim()) return 'Informe seu e-mail.';
    if (!isValidEmailFormat(email)) return 'Informe um e-mail válido.';
    if (!password) return 'Informe uma senha.';
    if (!passwordConfirmation) return 'Confirme sua senha.';
    if (passwordConfirmation !== password) return 'A confirmacao de senha deve ser igual a senha.';
    if (!phone.trim()) return 'Informe seu telefone.';
    if (!birthDate.trim()) return 'Informe sua data de nascimento.';
    return null;
  }

  function validateResponsibleRelationship() {
    if (!relationship) return 'Informe o vínculo com a pessoa assistida.';
    if (relationship === 'OUTRO' && !relationshipCustom.trim()) return 'Informe o parentesco personalizado.';
    if (!contactPreference) return 'Informe a preferência de contato.';
    return null;
  }

  function validateResponsibleAssistedPerson() {
    if (!assistedName.trim()) return 'Informe o nome da pessoa assistida.';
    if (!assistedBirthDate.trim()) return 'Informe a data de nascimento da pessoa assistida.';
    if (!dependencyLevel) return 'Informe o grau de dependencia.';
    if (!mobility) return 'Informe a mobilidade.';
    if (mobility === 'OUTRO' && !mobilityCustom.trim()) return 'Informe a mobilidade personalizada.';
    if (splitList(careNeeds).length === 0) return 'Informe ao menos uma necessidade de cuidado.';
    if (allergies.length === 0) return 'Informe se a pessoa assistida possui alergias.';
    if (showAllergyDetails && !allergyDetails.trim()) return 'Informe os detalhes da alergia.';
    if (foodRestrictions.length === 0) return 'Informe se ha restricoes alimentares.';
    if (showFoodRestrictionDetails && !foodRestrictionDetails.trim()) return 'Informe os detalhes da restrição alimentar.';
    return null;
  }

  function validateResponsibleCareDetails() {
    if (unformatCep(careAddress.cep).length !== 8) return 'Informe um CEP com 8 números.';
    if (!careAddress.rua.trim()) return 'Informe a rua do cuidado.';
    if (!careAddress.numero.trim()) return 'Informe o número do cuidado.';
    if (!careAddress.bairro.trim()) return 'Informe o bairro do cuidado.';
    if (!careAddress.cidade.trim()) return 'Informe a cidade do cuidado.';
    if (!careAddress.estado.trim()) return 'Informe o estado do cuidado.';
    if (useResponsibleAsEmergencyContact) return null;
    if (!emergencyName.trim() || !emergencyPhone.trim() || !emergencyRelation.trim()) {
      return 'Informe nome, telefone e vínculo do contato de emergência.';
    }
    return null;
  }

  function validateCaregiverAddress() {
    if (unformatCep(caregiverAddress.cep).length !== 8) return 'Informe um CEP com 8 números.';
    if (!caregiverAddress.rua.trim()) return 'Informe a rua.';
    if (!caregiverAddress.numero.trim()) return 'Informe o número.';
    if (!caregiverAddress.bairro.trim()) return 'Informe o bairro.';
    if (!caregiverAddress.cidade.trim()) return 'Informe a cidade.';
    if (!caregiverAddress.estado.trim()) return 'Informe o estado.';
    return null;
  }

  function validateCaregiverProfessionalData() {
    if (!experienceRange) return 'Informe seu tempo de experiência.';
    if (education.includes('OUTRO') && !educationCustom.trim()) return 'Informe a formação personalizada.';
    return null;
  }

  function validateCaregiverAvailabilityAndServices() {
    if (weekDays.length === 0) return 'Informe ao menos um dia de disponibilidade.';
    if (dayPeriods.length === 0) return 'Informe ao menos um período de disponibilidade.';
    if (usesCustomAvailability && (!availabilityStart.trim() || !availabilityEnd.trim())) {
      return 'Informe horário inicial e final.';
    }
    if (careModes.length === 0) return 'Informe ao menos uma modalidade de atendimento.';
    if (careModes.includes('OUTRO') && !careModeCustom.trim()) return 'Informe a modalidade personalizada.';
    if (services.length === 0) return 'Informe ao menos um serviço oferecido.';
    if (services.includes('OUTRO') && !serviceCustom.trim()) return 'Informe o serviço personalizado.';
    return null;
  }

  function validateCurrentStep() {
    if (step === 0) return null;
    if (step === 1) return validatePersonalData();
    if (role === 'family' && step === 2) return validateResponsibleRelationship();
    if (role === 'family' && step === 3) return validateResponsibleAssistedPerson();
    if (role === 'family' && step === 4) return validateResponsibleCareDetails();
    if (role === 'caregiver' && step === 2) return validateCaregiverAddress();
    if (role === 'caregiver' && step === 3) return validateCaregiverProfessionalData();
    if (role === 'caregiver' && step === 4) return validateCaregiverAvailabilityAndServices();
    return null;
  }

  function buildUser(tipoUsuario: UserBase['tipoUsuario']): UserBase {
    return {
      nome: fullName.trim(),
      cpf: unformatCpf(cpf),
      email: email.trim(),
      telefone: unformatPhone(phone),
      dataNascimento: birthDate.trim(),
      tipoUsuario,
      status: 'ACTIVE',
    };
  }

  function buildAssistedPerson(): AssistedPerson {
    return {
      nome: assistedName.trim(),
      dataNascimento: assistedBirthDate.trim(),
      cpf: assistedCpf.trim() ? unformatCpf(assistedCpf) : undefined,
      grauDependencia: dependencyLevel ?? 'NAO_SEI_INFORMAR',
      mobilidade: mobility ?? 'OUTRO',
      mobilidadePersonalizada: mobility === 'OUTRO' ? mobilityCustom.trim() : undefined,
      necessidadesCuidado: splitList(careNeeds),
      alergias: allergies.length > 0 ? allergies : undefined,
      detalhesAlergia: allergyDetails.trim() || undefined,
      medicamentos: medications.trim() || undefined,
      restricoesAlimentares: foodRestrictions.length > 0 ? foodRestrictions : undefined,
      detalhesRestricaoAlimentar: foodRestrictionDetails.trim() || undefined,
      observacoes: notes.trim() || undefined,
      enderecoCuidado: careAddress,
      contatoEmergencia: {
        nome: useResponsibleAsEmergencyContact ? fullName.trim() : emergencyName.trim(),
        telefone: useResponsibleAsEmergencyContact ? unformatPhone(phone) : unformatPhone(emergencyPhone),
        vinculo: useResponsibleAsEmergencyContact
          ? relationshipCustom.trim() || relationship || 'Responsável'
          : emergencyRelation.trim(),
        isResponsibleEmergencyContact: useResponsibleAsEmergencyContact,
      },
    };
  }

  function buildCaregiverProfile(): CaregiverProfile {
    return {
      tempoExperiencia: experienceRange ?? 'MENOS_DE_1_ANO',
      formacoes: education,
      formacaoPersonalizada: education.includes('OUTRO') ? educationCustom.trim() : undefined,
      biografia: bio.trim() || undefined,
      disponibilidade: {
        diasSemana: weekDays,
        periodos: dayPeriods,
        horariosEspecificos: usesCustomAvailability
          ? { inicio: availabilityStart.trim(), fim: availabilityEnd.trim() }
          : undefined,
        observacao: availabilityNote.trim() || undefined,
      },
      enderecoAtendimento: caregiverAddress,
      modalidadeAtendimento: careModes,
      modalidadePersonalizada: careModes.includes('OUTRO') ? careModeCustom.trim() : undefined,
      servicosOferecidos: services,
      servicoPersonalizado: services.includes('OUTRO') ? serviceCustom.trim() : undefined,
    };
  }

  async function handleNext() {
    if (isSubmitting || isFetchingCep || isFetchingCareCep) return;

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
            parentescoPadrao: relationship ?? undefined,
            parentescoPersonalizado: relationship === 'OUTRO' ? relationshipCustom.trim() : undefined,
            contatoPreferencial: contactPreference ?? undefined,
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
          profilePhoto,
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
        <BackButton disabled={screenBusy} />
        <BrandMark />
      </View>

      <View style={styles.heading}>
        <Text style={styles.title}>Criar sua conta</Text>
        <Text style={styles.subtitle}>{title}</Text>
      </View>

      {step === 0 ? <RoleSelector value={role} onChange={setRole} disabled={screenBusy} /> : null}

      <View style={styles.form}>
        {step === 1 ? (
          <>
            <AppTextInput required label="Nome completo" icon={User} placeholder="Maria da Silva" value={fullName} onChangeText={setFullName} autoCapitalize="words" disabled={formDisabled} />
            <AppTextInput required label="CPF" icon={IdCard} placeholder="000.000.000-00" value={cpf} onChangeText={(value) => setCpf(formatCpf(value))} keyboardType="number-pad" disabled={formDisabled} />
            <AppTextInput required label="E-mail" icon={Mail} placeholder="seu@email.com" value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" autoCorrect={false} visualState={email && !isValidEmailFormat(email) ? 'error' : 'default'} disabled={formDisabled} />
            <AppTextInput required label="Telefone" icon={Phone} placeholder="(00) 00000-0000" value={phone} onChangeText={(value) => setPhone(formatPhone(value))} keyboardType="phone-pad" disabled={formDisabled} />
            <DatePickerField required label="Data de nascimento" value={birthDate} onChange={setBirthDate} maxDate={today} disabled={formDisabled} />
            <AppTextInput required label="Senha" icon={Lock} placeholder="********" value={password} onChangeText={setPassword} secureTextEntry disabled={formDisabled} />
            <AppTextInput required label="Confirmar senha" icon={Lock} placeholder="********" value={passwordConfirmation} onChangeText={setPasswordConfirmation} secureTextEntry disabled={formDisabled} />
            {role === 'caregiver' ? (
              <View style={styles.photoSection}>
                <Text style={styles.photoTitle}>Foto de perfil <Text style={styles.optionalText}>(opcional)</Text></Text>
                <ProfileAvatar imageUrl={profilePhoto?.uri} initials={fullName.trim().slice(0, 2).toUpperCase() || 'CP'} />
                <PrimaryButton label={profilePhoto ? 'Trocar foto' : 'Adicionar foto'} variant="secondary" onPress={showPhotoOptions} disabled={formDisabled} />
                {profilePhoto ? <Pressable disabled={formDisabled} onPress={() => setProfilePhoto(null)}><Text style={styles.removePhoto}>Remover foto</Text></Pressable> : null}
              </View>
            ) : null}
          </>
        ) : null}

        {role === 'family' && step === 2 ? (
          <>
            <OptionGroup required label="Parentesco ou vínculo" options={relationshipOptions} value={relationship} onChange={(value) => setRelationship(value as Relationship)} disabled={formDisabled} />
            {relationship === 'OUTRO' ? (
              <AppTextInput required label="Parentesco personalizado" icon={HeartPulse} placeholder="Informe o vínculo" value={relationshipCustom} onChangeText={setRelationshipCustom} disabled={formDisabled} />
            ) : null}
            <OptionGroup required label="Preferência de contato" options={contactPreferenceOptions} value={contactPreference} onChange={(value) => setContactPreference(value as ContactPreference)} disabled={formDisabled} />
          </>
        ) : null}

        {role === 'family' && step === 3 ? (
          <>
            <AppTextInput required label="Nome da pessoa assistida" icon={User} placeholder="Nome completo" value={assistedName} onChangeText={setAssistedName} disabled={formDisabled} />
            <DatePickerField required label="Data de nascimento" value={assistedBirthDate} onChange={setAssistedBirthDate} maxDate={today} disabled={formDisabled} />
            <AppTextInput optional label="CPF da pessoa assistida" icon={IdCard} placeholder="000.000.000-00" value={assistedCpf} onChangeText={(value) => setAssistedCpf(formatCpf(value))} keyboardType="number-pad" disabled={formDisabled} />
            <OptionGroup required label="Grau de dependência" options={dependencyLevelOptions} value={dependencyLevel} onChange={(value) => setDependencyLevel(value as DependencyLevel)} disabled={formDisabled} />
            <OptionGroup required label="Mobilidade" options={mobilityOptions} value={mobility} onChange={(value) => setMobility(value as Mobility)} disabled={formDisabled} />
            {mobility === 'OUTRO' ? (
              <AppTextInput required label="Mobilidade personalizada" icon={HeartPulse} placeholder="Descreva a mobilidade" value={mobilityCustom} onChangeText={setMobilityCustom} disabled={formDisabled} />
            ) : null}
            <AppTextInput required label="Necessidades de cuidado" icon={HeartPulse} placeholder="Separadas por vírgula" value={careNeeds} onChangeText={setCareNeeds} disabled={formDisabled} />
            <OptionGroup required multiple label="Alergias" options={allergyOptions} value={allergies} onChange={(value) => setAllergies(normalizeExclusiveHealthOptions(value as Allergy[]))} disabled={formDisabled} />
            {showAllergyDetails ? (
              <AppTextInput required label="Detalhes da alergia" icon={HeartPulse} placeholder="Informe detalhes importantes" value={allergyDetails} onChangeText={setAllergyDetails} disabled={formDisabled} />
            ) : null}
            <AppTextInput optional label="Medicamentos" icon={HeartPulse} placeholder="Liste se houver" value={medications} onChangeText={setMedications} disabled={formDisabled} />
            <OptionGroup required multiple label="Restrições alimentares" options={foodRestrictionOptions} value={foodRestrictions} onChange={(value) => setFoodRestrictions(normalizeExclusiveHealthOptions(value as FoodRestriction[]))} disabled={formDisabled} />
            {showFoodRestrictionDetails ? (
              <AppTextInput required label="Detalhes da restrição alimentar" icon={HeartPulse} placeholder="Informe detalhes importantes" value={foodRestrictionDetails} onChangeText={setFoodRestrictionDetails} disabled={formDisabled} />
            ) : null}
            <AppTextInput optional label="Observações importantes" icon={HeartPulse} placeholder="Informações adicionais" value={notes} onChangeText={setNotes} disabled={formDisabled} />
          </>
        ) : null}

        {role === 'family' && step === 4 ? (
          <>
            <AppTextInput required label="CEP" icon={MapPin} placeholder="00000-000" value={careAddress.cep} onChangeText={(value) => updateCareAddress('cep', value)} keyboardType="number-pad" disabled={careAddressDisabled} />
            {careCepFeedback ? (
              <Text style={[
                styles.feedbackText,
                careCepFeedbackKind === 'success' && styles.successText,
                careCepFeedbackKind === 'error' && styles.errorText,
              ]}>
                {careCepFeedback}
              </Text>
            ) : null}
            <AppTextInput required label="Rua" icon={MapPin} placeholder="Rua do cuidado" value={careAddress.rua} onChangeText={(value) => updateCareAddress('rua', value)} disabled={careAddressDisabled} />
            <AppTextInput required label="Número" icon={MapPin} placeholder="123" value={careAddress.numero} onChangeText={(value) => updateCareAddress('numero', value)} disabled={careAddressDisabled} />
            <AppTextInput optional label="Complemento" icon={MapPin} placeholder="Apto, bloco ou casa" value={careAddress.complemento} onChangeText={(value) => updateCareAddress('complemento', value)} disabled={careAddressDisabled} />
            <AppTextInput required label="Bairro" icon={MapPin} placeholder="Bairro" value={careAddress.bairro} onChangeText={(value) => updateCareAddress('bairro', value)} disabled={careAddressDisabled} />
            <AppTextInput required label="Cidade" icon={MapPin} placeholder="Cidade" value={careAddress.cidade} onChangeText={(value) => updateCareAddress('cidade', value)} disabled={careAddressDisabled} />
            <AppTextInput required label="Estado" icon={MapPin} placeholder="UF" value={careAddress.estado} onChangeText={(value) => updateCareAddress('estado', value)} autoCapitalize="characters" disabled={careAddressDisabled} />
            <AppTextInput optional label="Ponto de referência" icon={MapPin} placeholder="Referência próxima" value={careAddress.pontoReferencia} onChangeText={(value) => updateCareAddress('pontoReferencia', value)} disabled={careAddressDisabled} />
            <Pressable
              accessibilityRole="checkbox"
              accessibilityState={{ checked: useResponsibleAsEmergencyContact }}
              disabled={formDisabled}
              onPress={() => setUseResponsibleAsEmergencyContact((value) => !value)}
              style={({ pressed }) => [styles.termsRow, formDisabled && styles.disabled, pressed && styles.pressed]}
            >
              <View style={[styles.checkbox, useResponsibleAsEmergencyContact && styles.checkboxChecked]}>
                {useResponsibleAsEmergencyContact ? <Check color={colors.primaryForeground} size={14} strokeWidth={3} /> : null}
              </View>
              <View style={styles.checkboxTextBlock}>
                <Text style={styles.termsText}>Sou o contato de emergência</Text>
                <Text style={styles.helperText}>Usaremos seu nome, telefone e vínculo como contato principal.</Text>
              </View>
            </Pressable>
            {useResponsibleAsEmergencyContact ? (
              <View style={styles.emergencyPreview}>
                <Text style={styles.previewText}>{fullName || 'Nome do responsável'}</Text>
                <Text style={styles.previewText}>{phone || 'Telefone do responsável'}</Text>
                <Text style={styles.previewText}>{relationshipCustom || relationship || 'Vínculo informado'}</Text>
              </View>
            ) : (
              <>
                <AppTextInput required label="Contato de emergência" icon={User} placeholder="Nome completo" value={emergencyName} onChangeText={setEmergencyName} disabled={formDisabled} />
                <AppTextInput required label="Telefone de emergência" icon={Phone} placeholder="(00) 00000-0000" value={emergencyPhone} onChangeText={(value) => setEmergencyPhone(formatPhone(value))} keyboardType="phone-pad" disabled={formDisabled} />
                <AppTextInput required label="Vínculo do contato" icon={HeartPulse} placeholder="Irmã, vizinho, amigo..." value={emergencyRelation} onChangeText={setEmergencyRelation} disabled={formDisabled} />
              </>
            )}
          </>
        ) : null}

        {role === 'caregiver' && step === 2 ? (
          <>
            <AppTextInput required label="CEP" icon={MapPin} placeholder="00000-000" value={caregiverAddress.cep} onChangeText={(value) => updateCaregiverAddress('cep', value)} keyboardType="number-pad" disabled={caregiverAddressDisabled} />
            {cepFeedback ? (
              <Text style={[
                styles.feedbackText,
                cepFeedbackKind === 'success' && styles.successText,
                cepFeedbackKind === 'error' && styles.errorText,
              ]}>
                {cepFeedback}
              </Text>
            ) : null}
            <AppTextInput required label="Rua" icon={MapPin} placeholder="Rua" value={caregiverAddress.rua} onChangeText={(value) => updateCaregiverAddress('rua', value)} disabled={caregiverAddressDisabled} />
            <AppTextInput required label="Número" icon={MapPin} placeholder="123" value={caregiverAddress.numero} onChangeText={(value) => updateCaregiverAddress('numero', value)} disabled={caregiverAddressDisabled} />
            <AppTextInput optional label="Complemento" icon={MapPin} placeholder="Apto, bloco ou casa" value={caregiverAddress.complemento} onChangeText={(value) => updateCaregiverAddress('complemento', value)} disabled={caregiverAddressDisabled} />
            <AppTextInput required label="Bairro" icon={MapPin} placeholder="Bairro" value={caregiverAddress.bairro} onChangeText={(value) => updateCaregiverAddress('bairro', value)} disabled={caregiverAddressDisabled} />
            <AppTextInput required label="Cidade" icon={MapPin} placeholder="Cidade" value={caregiverAddress.cidade} onChangeText={(value) => updateCaregiverAddress('cidade', value)} disabled={caregiverAddressDisabled} />
            <AppTextInput required label="Estado" icon={MapPin} placeholder="UF" value={caregiverAddress.estado} onChangeText={(value) => updateCaregiverAddress('estado', value)} autoCapitalize="characters" disabled={caregiverAddressDisabled} />
            <AppTextInput optional label="Ponto de referência" icon={MapPin} placeholder="Referência próxima" value={caregiverAddress.pontoReferencia} onChangeText={(value) => updateCaregiverAddress('pontoReferencia', value)} disabled={caregiverAddressDisabled} />
          </>
        ) : null}

        {role === 'caregiver' && step === 3 ? (
          <>
            <OptionGroup required label="Experiência" options={caregiverExperienceRangeOptions} value={experienceRange} onChange={(value) => setExperienceRange(value as CaregiverExperienceRange)} disabled={formDisabled} />
            <OptionGroup multiple optional label="Formação" options={caregiverEducationOptions} value={education} onChange={(value) => setEducation(value as CaregiverEducation[])} disabled={formDisabled} />
            {education.includes('OUTRO') ? (
              <AppTextInput required label="Formação personalizada" icon={HeartPulse} placeholder="Informe sua formação" value={educationCustom} onChangeText={setEducationCustom} disabled={formDisabled} />
            ) : null}
            <AppTextInput optional label="Biografia profissional" icon={HeartPulse} placeholder="Apresentação breve" value={bio} onChangeText={setBio} multiline numberOfLines={3} textAlignVertical="top" disabled={formDisabled} />
          </>
        ) : null}

        {role === 'caregiver' && step === 4 ? (
          <>
            <OptionGroup required multiple label="Dias disponíveis" options={weekDayOptions} value={weekDays} onChange={(value) => setWeekDays(value as WeekDay[])} disabled={formDisabled} />
            <OptionGroup required multiple label="Períodos disponíveis" options={dayPeriodOptions} value={dayPeriods} onChange={(value) => setDayPeriods(value as DayPeriod[])} disabled={formDisabled} />
            {usesCustomAvailability ? (
              <View style={styles.inlineFields}>
                <AppTextInput required label="Horário inicial" icon={Calendar} placeholder="08:00" value={availabilityStart} onChangeText={setAvailabilityStart} disabled={formDisabled} />
                <AppTextInput required label="Horário final" icon={Calendar} placeholder="18:00" value={availabilityEnd} onChangeText={setAvailabilityEnd} disabled={formDisabled} />
              </View>
            ) : null}
            <AppTextInput optional label="Observação de disponibilidade" icon={Calendar} placeholder="Detalhes de agenda" value={availabilityNote} onChangeText={setAvailabilityNote} disabled={formDisabled} />
            <OptionGroup required multiple label="Modalidade de atendimento" options={careModalityOptions} value={careModes} onChange={(value) => setCareModes(value as CareModality[])} disabled={formDisabled} />
            {careModes.includes('OUTRO') ? (
              <AppTextInput required label="Modalidade personalizada" icon={HeartPulse} placeholder="Informe a modalidade" value={careModeCustom} onChangeText={setCareModeCustom} disabled={formDisabled} />
            ) : null}
            <OptionGroup required multiple label="Serviços oferecidos" options={caregiverServiceOptions} value={services} onChange={(value) => setServices(value as CaregiverService[])} disabled={formDisabled} />
            {services.includes('OUTRO') ? (
              <AppTextInput required label="Serviço personalizado" icon={HeartPulse} placeholder="Informe o serviço" value={serviceCustom} onChangeText={setServiceCustom} disabled={formDisabled} />
            ) : null}
          </>
        ) : null}

        {step === maxStep ? (
          <Pressable
            accessibilityRole="checkbox"
            accessibilityState={{ checked: acceptedTerms }}
            disabled={formDisabled}
            onPress={() => setAcceptedTerms((value) => !value)}
            style={({ pressed }) => [styles.termsRow, formDisabled && styles.disabled, pressed && styles.pressed]}
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
            <PrimaryButton label="Voltar" variant="secondary" onPress={() => setStep((current) => current - 1)} disabled={screenBusy} />
          ) : null}
          <PrimaryButton
            label={step < maxStep ? 'Continuar' : isSubmitting ? 'Criando conta...' : 'Criar conta'}
            onPress={handleNext}
            disabled={isSubmitting || isFetchingCep || isFetchingCareCep}
            loading={isSubmitting}
          />
        </View>
      </View>

      <Text style={styles.footerText}>
        Já tem conta?{' '}
        <Text
          accessibilityRole="link"
          onPress={screenBusy ? undefined : () => router.push('/login')}
          style={[styles.footerLink, screenBusy && styles.disabledLink]}
        >
          Entrar
        </Text>
      </Text>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.lg,
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
  photoSection: { alignItems: 'center', gap: spacing.sm, paddingVertical: spacing.sm },
  photoTitle: { fontFamily: fontFamily.semiBold, fontSize: 14, color: colors.foreground },
  optionalText: { fontFamily: fontFamily.regular, color: colors.mutedForeground },
  removePhoto: { fontFamily: fontFamily.semiBold, fontSize: 13, color: colors.destructive, padding: spacing.sm },
  actions: {
    gap: spacing.md,
  },
  inlineFields: {
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
  disabled: {
    opacity: 0.62,
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
  checkboxTextBlock: {
    flex: 1,
    gap: spacing.xxs,
  },
  helperText: {
    fontFamily: fontFamily.regular,
    fontSize: 11.5,
    lineHeight: 17,
    color: colors.mutedForeground,
  },
  emergencyPreview: {
    gap: spacing.xs,
    borderRadius: radii.lg,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.secondary,
    padding: spacing.md,
  },
  previewText: {
    fontFamily: fontFamily.medium,
    fontSize: 12,
    lineHeight: 18,
    color: colors.secondaryForeground,
  },
  termsLink: {
    fontFamily: fontFamily.semiBold,
    color: colors.primary,
  },
  feedbackText: {
    fontFamily: fontFamily.medium,
    fontSize: 12,
    lineHeight: 18,
    color: colors.mutedForeground,
  },
  successText: {
    color: colors.mintForeground,
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
  disabledLink: {
    color: colors.mutedForeground,
    opacity: 0.55,
  },
});
