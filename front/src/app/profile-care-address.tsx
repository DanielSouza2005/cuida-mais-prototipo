import { useEffect, useState } from 'react';
import { Home, MapPin, Save } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { ApiError } from '@/services/api';
import { CepError, getAddressByCep } from '@/services/cepService';
import { getMyProfile, updateCareAddress } from '@/services/profileService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { Address } from '@/types/auth';
import { formatCep, unformatCep } from '@/utils/masks';

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

export default function ProfileCareAddressScreen() {
  const [assistedPersonId, setAssistedPersonId] = useState('');
  const [address, setAddress] = useState<Address>(emptyAddress);
  const [feedback, setFeedback] = useState<string | null>(null);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [isSearchingCep, setIsSearchingCep] = useState(false);

  useEffect(() => {
    let active = true;
    getMyProfile()
      .then((profile) => {
        if (!active) return;
        const assistedPerson = profile.assistedPersons?.[0];
        if (!assistedPerson) {
          setFeedback('Nenhuma pessoa assistida encontrada para este perfil.');
          return;
        }

        setAssistedPersonId(assistedPerson.id);
        setAddress({
          ...emptyAddress,
          ...(assistedPerson.enderecoCuidado ?? {}),
          cep: formatCep(assistedPerson.enderecoCuidado?.cep ?? ''),
        });
      })
      .catch((error) => setFeedback(error instanceof ApiError ? error.message : 'Não foi possível carregar o endereço do cuidado.'))
      .finally(() => active && setIsLoading(false));

    return () => {
      active = false;
    };
  }, []);

  function updateAddress(field: keyof Address, value: string) {
    setAddress((current) => ({ ...current, [field]: field === 'cep' ? formatCep(value) : value }));
  }

  async function searchCep() {
    if (unformatCep(address.cep).length !== 8) return;

    try {
      setIsSearchingCep(true);
      setFeedback('Consultando CEP...');
      setIsSuccess(false);
      const found = await getAddressByCep(address.cep);
      setAddress((current) => ({
        ...current,
        cep: formatCep(found.cep ?? current.cep),
        rua: found.rua ?? current.rua,
        bairro: found.bairro ?? current.bairro,
        cidade: found.cidade ?? current.cidade,
        estado: found.estado ?? current.estado,
        complemento: current.complemento?.trim() ? current.complemento : found.complemento ?? current.complemento,
        numero: current.numero,
      }));
      setFeedback('Endereço preenchido pelo CEP. Você pode editar se precisar.');
      setIsSuccess(true);
    } catch (error) {
      setFeedback(error instanceof CepError ? error.message : 'Não foi possível consultar o CEP.');
      setIsSuccess(false);
    } finally {
      setIsSearchingCep(false);
    }
  }

  async function handleSave() {
    setFeedback(null);
    setIsSuccess(false);

    if (!assistedPersonId) return setFeedback('Pessoa assistida não encontrada.');
    if (unformatCep(address.cep).length !== 8) return setFeedback('Informe um CEP com 8 números.');
    if (!address.rua.trim()) return setFeedback('Informe a rua do cuidado.');
    if (!address.numero.trim()) return setFeedback('Informe o número do cuidado.');
    if (!address.bairro.trim()) return setFeedback('Informe o bairro do cuidado.');
    if (!address.cidade.trim()) return setFeedback('Informe a cidade do cuidado.');
    if (!address.estado.trim()) return setFeedback('Informe o estado do cuidado.');

    try {
      setIsSaving(true);
      const response = await updateCareAddress(assistedPersonId, address);
      setFeedback(response.message);
      setIsSuccess(true);
    } catch (error) {
      setFeedback(error instanceof ApiError ? error.message : 'Não foi possível salvar o endereço do cuidado.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <AppHeader showBack title="Endereço do cuidado" subtitle="Local onde o cuidado será realizado" />
      <View style={styles.card}>
        <AppTextInput required label="CEP" icon={MapPin} placeholder="00000-000" value={address.cep} onChangeText={(value) => updateAddress('cep', value)} onBlur={searchCep} keyboardType="number-pad" editable={!isLoading} />
        <AppTextInput required label="Rua" icon={Home} placeholder="Rua do cuidado" value={address.rua} onChangeText={(value) => updateAddress('rua', value)} editable={!isLoading} />
        <AppTextInput required label="Número" icon={Home} placeholder="123" value={address.numero} onChangeText={(value) => updateAddress('numero', value)} editable={!isLoading} />
        <AppTextInput optional label="Complemento" icon={Home} placeholder="Apto, bloco ou casa" value={address.complemento} onChangeText={(value) => updateAddress('complemento', value)} editable={!isLoading} />
        <AppTextInput required label="Bairro" icon={MapPin} placeholder="Bairro" value={address.bairro} onChangeText={(value) => updateAddress('bairro', value)} editable={!isLoading} />
        <AppTextInput required label="Cidade" icon={MapPin} placeholder="Cidade" value={address.cidade} onChangeText={(value) => updateAddress('cidade', value)} editable={!isLoading} />
        <AppTextInput required label="Estado" icon={MapPin} placeholder="UF" value={address.estado} onChangeText={(value) => updateAddress('estado', value.toUpperCase().slice(0, 2))} autoCapitalize="characters" editable={!isLoading} />
        <AppTextInput optional label="Ponto de referência" icon={MapPin} placeholder="Referência próxima" value={address.pontoReferencia} onChangeText={(value) => updateAddress('pontoReferencia', value)} editable={!isLoading} />
        {feedback ? <Text style={[styles.feedback, isSuccess && styles.success]}>{feedback}</Text> : null}
        <PrimaryButton label={isSaving ? 'Salvando...' : isSearchingCep ? 'Consultando CEP...' : 'Salvar alterações'} icon={Save} onPress={handleSave} disabled={isLoading || isSaving || isSearchingCep} />
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.xxl,
    gap: spacing.xl,
  },
  card: {
    gap: spacing.lg,
    padding: spacing.xl,
    borderRadius: radii.xxl,
    backgroundColor: colors.card,
    borderWidth: 1,
    borderColor: colors.border,
    ...shadows.card,
  },
  feedback: {
    fontFamily: fontFamily.medium,
    fontSize: 12,
    lineHeight: 18,
    color: colors.destructive,
  },
  success: {
    color: colors.mintForeground,
  },
});
