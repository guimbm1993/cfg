jest.mock('@angular/router');

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';

import { ProdutoService } from '../service/produto.service';
import { IProduto, Produto } from '../produto.model';
import { IEmpresa } from 'app/entities/empresa/empresa.model';
import { EmpresaService } from 'app/entities/empresa/service/empresa.service';

import { ProdutoUpdateComponent } from './produto-update.component';

describe('Component Tests', () => {
  describe('Produto Management Update Component', () => {
    let comp: ProdutoUpdateComponent;
    let fixture: ComponentFixture<ProdutoUpdateComponent>;
    let activatedRoute: ActivatedRoute;
    let produtoService: ProdutoService;
    let empresaService: EmpresaService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        declarations: [ProdutoUpdateComponent],
        providers: [FormBuilder, ActivatedRoute],
      })
        .overrideTemplate(ProdutoUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(ProdutoUpdateComponent);
      activatedRoute = TestBed.inject(ActivatedRoute);
      produtoService = TestBed.inject(ProdutoService);
      empresaService = TestBed.inject(EmpresaService);

      comp = fixture.componentInstance;
    });

    describe('ngOnInit', () => {
      it('Should call Empresa query and add missing value', () => {
        const produto: IProduto = { id: 456 };
        const empresa: IEmpresa = { id: 66061 };
        produto.empresa = empresa;

        const empresaCollection: IEmpresa[] = [{ id: 29387 }];
        spyOn(empresaService, 'query').and.returnValue(of(new HttpResponse({ body: empresaCollection })));
        const additionalEmpresas = [empresa];
        const expectedCollection: IEmpresa[] = [...additionalEmpresas, ...empresaCollection];
        spyOn(empresaService, 'addEmpresaToCollectionIfMissing').and.returnValue(expectedCollection);

        activatedRoute.data = of({ produto });
        comp.ngOnInit();

        expect(empresaService.query).toHaveBeenCalled();
        expect(empresaService.addEmpresaToCollectionIfMissing).toHaveBeenCalledWith(empresaCollection, ...additionalEmpresas);
        expect(comp.empresasSharedCollection).toEqual(expectedCollection);
      });

      it('Should update editForm', () => {
        const produto: IProduto = { id: 456 };
        const empresa: IEmpresa = { id: 8416 };
        produto.empresa = empresa;

        activatedRoute.data = of({ produto });
        comp.ngOnInit();

        expect(comp.editForm.value).toEqual(expect.objectContaining(produto));
        expect(comp.empresasSharedCollection).toContain(empresa);
      });
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const produto = { id: 123 };
        spyOn(produtoService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ produto });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: produto }));
        saveSubject.complete();

        // THEN
        expect(comp.previousState).toHaveBeenCalled();
        expect(produtoService.update).toHaveBeenCalledWith(produto);
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const produto = new Produto();
        spyOn(produtoService, 'create').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ produto });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: produto }));
        saveSubject.complete();

        // THEN
        expect(produtoService.create).toHaveBeenCalledWith(produto);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).toHaveBeenCalled();
      });

      it('Should set isSaving to false on error', () => {
        // GIVEN
        const saveSubject = new Subject();
        const produto = { id: 123 };
        spyOn(produtoService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ produto });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.error('This is an error!');

        // THEN
        expect(produtoService.update).toHaveBeenCalledWith(produto);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).not.toHaveBeenCalled();
      });
    });

    describe('Tracking relationships identifiers', () => {
      describe('trackEmpresaById', () => {
        it('Should return tracked Empresa primary key', () => {
          const entity = { id: 123 };
          const trackResult = comp.trackEmpresaById(0, entity);
          expect(trackResult).toEqual(entity.id);
        });
      });
    });
  });
});
