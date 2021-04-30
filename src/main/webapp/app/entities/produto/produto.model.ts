import { IEmpresa } from 'app/entities/empresa/empresa.model';

export interface IProduto {
  id?: number;
  nome?: string | null;
  categoria?: string | null;
  finalidade?: string | null;
  descricao?: string | null;
  ncm?: string | null;
  empresa?: IEmpresa | null;
}

export class Produto implements IProduto {
  constructor(
    public id?: number,
    public nome?: string | null,
    public categoria?: string | null,
    public finalidade?: string | null,
    public descricao?: string | null,
    public ncm?: string | null,
    public empresa?: IEmpresa | null
  ) {}
}

export function getProdutoIdentifier(produto: IProduto): number | undefined {
  return produto.id;
}
