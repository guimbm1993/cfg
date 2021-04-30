import { IProduto } from 'app/entities/produto/produto.model';

export interface IEmpresa {
  id?: number;
  nome?: string | null;
  cnpj?: string | null;
  localidade?: string | null;
  segmento?: string | null;
  produtos?: IProduto[] | null;
}

export class Empresa implements IEmpresa {
  constructor(
    public id?: number,
    public nome?: string | null,
    public cnpj?: string | null,
    public localidade?: string | null,
    public segmento?: string | null,
    public produtos?: IProduto[] | null
  ) {}
}

export function getEmpresaIdentifier(empresa: IEmpresa): number | undefined {
  return empresa.id;
}
