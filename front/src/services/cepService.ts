import type { Address } from '@/types/auth';
import { unformatCep } from '@/utils/masks';

type ViaCepResponse = {
  erro?: boolean;
  cep?: string;
  logradouro?: string;
  complemento?: string;
  bairro?: string;
  localidade?: string;
  uf?: string;
};

export class CepError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'CepError';
  }
}

export async function getAddressByCep(cep: string): Promise<Partial<Address>> {
  const cleanCep = unformatCep(cep);

  if (cleanCep.length !== 8) {
    throw new CepError('Informe um CEP com 8 numeros.');
  }

  let response: Response;
  try {
    response = await fetch(`https://viacep.com.br/ws/${cleanCep}/json/`);
  } catch {
    throw new CepError('Nao foi possivel consultar o CEP agora.');
  }

  if (!response.ok) {
    throw new CepError('Nao foi possivel consultar o CEP agora.');
  }

  const payload = await response.json() as ViaCepResponse;
  if (payload.erro) {
    throw new CepError('CEP nao encontrado. Confira os numeros ou preencha manualmente.');
  }

  return {
    cep: payload.cep ?? cep,
    rua: payload.logradouro ?? '',
    complemento: payload.complemento ?? '',
    bairro: payload.bairro ?? '',
    cidade: payload.localidade ?? '',
    estado: payload.uf ?? '',
  };
}
