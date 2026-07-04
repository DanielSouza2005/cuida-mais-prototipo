import { useEffect, useState } from 'react';
import { Home, MapPin, Save } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { ApiError } from '@/services/api';
import { CepError, getAddressByCep } from '@/services/cepService';
import { getMyProfile, updateCaregiverAddress } from '@/services/profileService';
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

export default function ProfileCaregiverAddressScreen() {
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
        setAddress({
          ...emptyAddress,
          ...(profile.caregiverProfile?.enderecoAtendimento ?? {}),
          cep: formatCep(profile.caregiverProfile?.enderecoAtendimento?.cep ?? ''),
        });
      })
      .catch((error) => setFeedback(error instanceof ApiError ? error.message : 'Nao foi possivel carregar o endereco.'))
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
      const found = await getAddressByCep(address.cep);
      setAddress((current) => ({
        ...current,
        ...found,
        cep: formatCep(found.cep ?? current.cep),
        numero: current.numero,
      }));
    } catch (error) {
      setFeedback(error instanceof CepError ? error.message : 'Nao foi possivel consultar o CEP.');
    } finally {
      setIsSearchingCep(false);
    }
  }

  async function handleSave() {
    setFeedback(null);
    setIsSuccess(false);

    if (unformatCep(address.cep).length !== 8) return setFeedback('Informe um CEP com 8 numeros.');
    if (!address.rua.trim()) return setFeedback('Informe a rua.');
    if (!address.numero.trim()) return setFeedback('Informe o numero.');
    if (!address.bairro.trim()) return setFeedback('Informe o bairro.');
    if (!address.cidade.trim()) return setFeedback('Informe a cidade.');
    if (!address.estado.trim()) return setFeedback('Informe o estado.');

    try {
      setIsSaving(true);
      const response = await updateCaregiverAddress(address);
      setFeedback(response.message);
      setIsSuccess(true);
    } catch (error) {
      setFeedback(error instanceof ApiError ? error.message : 'Nao foi possivel salvar o endereco.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <AppHeader showBack title="Endereco" subtitle="Localizacao e dados de endereco" />
      <View style={styles.card}>
        <AppTextInput required label="CEP" icon={MapPin} placeholder="00000-000" value={address.cep} onChangeText={(value) => updateAddress('cep', value)} onBlur={searchCep} keyboardType="number-pad" editable={!isLoading} />
        <AppTextInput required label="Rua" icon={Home} placeholder="Rua" value={address.rua} onChangeText={(value) => updateAddress('rua', value)} editable={!isLoading} />
        <AppTextInput required label="Numero" icon={Home} placeholder="123" value={address.numero} onChangeText={(value) => updateAddress('numero', value)} editable={!isLoading} />
        <AppTextInput optional label="Complemento" icon={Home} placeholder="Apto, bloco ou casa" value={address.complemento} onChangeText={(value) => updateAddress('complemento', value)} editable={!isLoading} />
        <AppTextInput required label="Bairro" icon={MapPin} placeholder="Bairro" value={address.bairro} onChangeText={(value) => updateAddress('bairro', value)} editable={!isLoading} />
        <AppTextInput required label="Cidade" icon={MapPin} placeholder="Cidade" value={address.cidade} onChangeText={(value) => updateAddress('cidade', value)} editable={!isLoading} />
        <AppTextInput required label="Estado" icon={MapPin} placeholder="UF" value={address.estado} onChangeText={(value) => updateAddress('estado', value.toUpperCase().slice(0, 2))} autoCapitalize="characters" editable={!isLoading} />
        <AppTextInput optional label="Ponto de referencia" icon={MapPin} placeholder="Referencia proxima" value={address.pontoReferencia} onChangeText={(value) => updateAddress('pontoReferencia', value)} editable={!isLoading} />
        {feedback ? <Text style={[styles.feedback, isSuccess && styles.success]}>{feedback}</Text> : null}
        <PrimaryButton label={isSaving ? 'Salvando...' : isSearchingCep ? 'Consultando CEP...' : 'Salvar alteracoes'} icon={Save} onPress={handleSave} disabled={isLoading || isSaving || isSearchingCep} />
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
