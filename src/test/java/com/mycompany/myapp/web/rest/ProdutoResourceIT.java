package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Produto;
import com.mycompany.myapp.repository.ProdutoRepository;
import com.mycompany.myapp.service.dto.ProdutoDTO;
import com.mycompany.myapp.service.mapper.ProdutoMapper;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ProdutoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ProdutoResourceIT {

    private static final String DEFAULT_NOME = "AAAAAAAAAA";
    private static final String UPDATED_NOME = "BBBBBBBBBB";

    private static final String DEFAULT_CATEGORIA = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORIA = "BBBBBBBBBB";

    private static final String DEFAULT_FINALIDADE = "AAAAAAAAAA";
    private static final String UPDATED_FINALIDADE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRICAO = "AAAAAAAAAA";
    private static final String UPDATED_DESCRICAO = "BBBBBBBBBB";

    private static final String DEFAULT_NCM = "AAAAAAAAAA";
    private static final String UPDATED_NCM = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/produtos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoMapper produtoMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProdutoMockMvc;

    private Produto produto;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Produto createEntity(EntityManager em) {
        Produto produto = new Produto()
            .nome(DEFAULT_NOME)
            .categoria(DEFAULT_CATEGORIA)
            .finalidade(DEFAULT_FINALIDADE)
            .descricao(DEFAULT_DESCRICAO)
            .ncm(DEFAULT_NCM);
        return produto;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Produto createUpdatedEntity(EntityManager em) {
        Produto produto = new Produto()
            .nome(UPDATED_NOME)
            .categoria(UPDATED_CATEGORIA)
            .finalidade(UPDATED_FINALIDADE)
            .descricao(UPDATED_DESCRICAO)
            .ncm(UPDATED_NCM);
        return produto;
    }

    @BeforeEach
    public void initTest() {
        produto = createEntity(em);
    }

    @Test
    @Transactional
    void createProduto() throws Exception {
        int databaseSizeBeforeCreate = produtoRepository.findAll().size();
        // Create the Produto
        ProdutoDTO produtoDTO = produtoMapper.toDto(produto);
        restProdutoMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(produtoDTO))
            )
            .andExpect(status().isCreated());

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll();
        assertThat(produtoList).hasSize(databaseSizeBeforeCreate + 1);
        Produto testProduto = produtoList.get(produtoList.size() - 1);
        assertThat(testProduto.getNome()).isEqualTo(DEFAULT_NOME);
        assertThat(testProduto.getCategoria()).isEqualTo(DEFAULT_CATEGORIA);
        assertThat(testProduto.getFinalidade()).isEqualTo(DEFAULT_FINALIDADE);
        assertThat(testProduto.getDescricao()).isEqualTo(DEFAULT_DESCRICAO);
        assertThat(testProduto.getNcm()).isEqualTo(DEFAULT_NCM);
    }

    @Test
    @Transactional
    void createProdutoWithExistingId() throws Exception {
        // Create the Produto with an existing ID
        produto.setId(1L);
        ProdutoDTO produtoDTO = produtoMapper.toDto(produto);

        int databaseSizeBeforeCreate = produtoRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restProdutoMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(produtoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll();
        assertThat(produtoList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllProdutos() throws Exception {
        // Initialize the database
        produtoRepository.saveAndFlush(produto);

        // Get all the produtoList
        restProdutoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(produto.getId().intValue())))
            .andExpect(jsonPath("$.[*].nome").value(hasItem(DEFAULT_NOME)))
            .andExpect(jsonPath("$.[*].categoria").value(hasItem(DEFAULT_CATEGORIA)))
            .andExpect(jsonPath("$.[*].finalidade").value(hasItem(DEFAULT_FINALIDADE)))
            .andExpect(jsonPath("$.[*].descricao").value(hasItem(DEFAULT_DESCRICAO)))
            .andExpect(jsonPath("$.[*].ncm").value(hasItem(DEFAULT_NCM)));
    }

    @Test
    @Transactional
    void getProduto() throws Exception {
        // Initialize the database
        produtoRepository.saveAndFlush(produto);

        // Get the produto
        restProdutoMockMvc
            .perform(get(ENTITY_API_URL_ID, produto.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(produto.getId().intValue()))
            .andExpect(jsonPath("$.nome").value(DEFAULT_NOME))
            .andExpect(jsonPath("$.categoria").value(DEFAULT_CATEGORIA))
            .andExpect(jsonPath("$.finalidade").value(DEFAULT_FINALIDADE))
            .andExpect(jsonPath("$.descricao").value(DEFAULT_DESCRICAO))
            .andExpect(jsonPath("$.ncm").value(DEFAULT_NCM));
    }

    @Test
    @Transactional
    void getNonExistingProduto() throws Exception {
        // Get the produto
        restProdutoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewProduto() throws Exception {
        // Initialize the database
        produtoRepository.saveAndFlush(produto);

        int databaseSizeBeforeUpdate = produtoRepository.findAll().size();

        // Update the produto
        Produto updatedProduto = produtoRepository.findById(produto.getId()).get();
        // Disconnect from session so that the updates on updatedProduto are not directly saved in db
        em.detach(updatedProduto);
        updatedProduto
            .nome(UPDATED_NOME)
            .categoria(UPDATED_CATEGORIA)
            .finalidade(UPDATED_FINALIDADE)
            .descricao(UPDATED_DESCRICAO)
            .ncm(UPDATED_NCM);
        ProdutoDTO produtoDTO = produtoMapper.toDto(updatedProduto);

        restProdutoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, produtoDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(produtoDTO))
            )
            .andExpect(status().isOk());

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll();
        assertThat(produtoList).hasSize(databaseSizeBeforeUpdate);
        Produto testProduto = produtoList.get(produtoList.size() - 1);
        assertThat(testProduto.getNome()).isEqualTo(UPDATED_NOME);
        assertThat(testProduto.getCategoria()).isEqualTo(UPDATED_CATEGORIA);
        assertThat(testProduto.getFinalidade()).isEqualTo(UPDATED_FINALIDADE);
        assertThat(testProduto.getDescricao()).isEqualTo(UPDATED_DESCRICAO);
        assertThat(testProduto.getNcm()).isEqualTo(UPDATED_NCM);
    }

    @Test
    @Transactional
    void putNonExistingProduto() throws Exception {
        int databaseSizeBeforeUpdate = produtoRepository.findAll().size();
        produto.setId(count.incrementAndGet());

        // Create the Produto
        ProdutoDTO produtoDTO = produtoMapper.toDto(produto);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProdutoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, produtoDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(produtoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll();
        assertThat(produtoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchProduto() throws Exception {
        int databaseSizeBeforeUpdate = produtoRepository.findAll().size();
        produto.setId(count.incrementAndGet());

        // Create the Produto
        ProdutoDTO produtoDTO = produtoMapper.toDto(produto);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProdutoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(produtoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll();
        assertThat(produtoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamProduto() throws Exception {
        int databaseSizeBeforeUpdate = produtoRepository.findAll().size();
        produto.setId(count.incrementAndGet());

        // Create the Produto
        ProdutoDTO produtoDTO = produtoMapper.toDto(produto);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProdutoMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(produtoDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll();
        assertThat(produtoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateProdutoWithPatch() throws Exception {
        // Initialize the database
        produtoRepository.saveAndFlush(produto);

        int databaseSizeBeforeUpdate = produtoRepository.findAll().size();

        // Update the produto using partial update
        Produto partialUpdatedProduto = new Produto();
        partialUpdatedProduto.setId(produto.getId());

        partialUpdatedProduto.categoria(UPDATED_CATEGORIA).descricao(UPDATED_DESCRICAO).ncm(UPDATED_NCM);

        restProdutoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProduto.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedProduto))
            )
            .andExpect(status().isOk());

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll();
        assertThat(produtoList).hasSize(databaseSizeBeforeUpdate);
        Produto testProduto = produtoList.get(produtoList.size() - 1);
        assertThat(testProduto.getNome()).isEqualTo(DEFAULT_NOME);
        assertThat(testProduto.getCategoria()).isEqualTo(UPDATED_CATEGORIA);
        assertThat(testProduto.getFinalidade()).isEqualTo(DEFAULT_FINALIDADE);
        assertThat(testProduto.getDescricao()).isEqualTo(UPDATED_DESCRICAO);
        assertThat(testProduto.getNcm()).isEqualTo(UPDATED_NCM);
    }

    @Test
    @Transactional
    void fullUpdateProdutoWithPatch() throws Exception {
        // Initialize the database
        produtoRepository.saveAndFlush(produto);

        int databaseSizeBeforeUpdate = produtoRepository.findAll().size();

        // Update the produto using partial update
        Produto partialUpdatedProduto = new Produto();
        partialUpdatedProduto.setId(produto.getId());

        partialUpdatedProduto
            .nome(UPDATED_NOME)
            .categoria(UPDATED_CATEGORIA)
            .finalidade(UPDATED_FINALIDADE)
            .descricao(UPDATED_DESCRICAO)
            .ncm(UPDATED_NCM);

        restProdutoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProduto.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedProduto))
            )
            .andExpect(status().isOk());

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll();
        assertThat(produtoList).hasSize(databaseSizeBeforeUpdate);
        Produto testProduto = produtoList.get(produtoList.size() - 1);
        assertThat(testProduto.getNome()).isEqualTo(UPDATED_NOME);
        assertThat(testProduto.getCategoria()).isEqualTo(UPDATED_CATEGORIA);
        assertThat(testProduto.getFinalidade()).isEqualTo(UPDATED_FINALIDADE);
        assertThat(testProduto.getDescricao()).isEqualTo(UPDATED_DESCRICAO);
        assertThat(testProduto.getNcm()).isEqualTo(UPDATED_NCM);
    }

    @Test
    @Transactional
    void patchNonExistingProduto() throws Exception {
        int databaseSizeBeforeUpdate = produtoRepository.findAll().size();
        produto.setId(count.incrementAndGet());

        // Create the Produto
        ProdutoDTO produtoDTO = produtoMapper.toDto(produto);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProdutoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, produtoDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(produtoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll();
        assertThat(produtoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchProduto() throws Exception {
        int databaseSizeBeforeUpdate = produtoRepository.findAll().size();
        produto.setId(count.incrementAndGet());

        // Create the Produto
        ProdutoDTO produtoDTO = produtoMapper.toDto(produto);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProdutoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(produtoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll();
        assertThat(produtoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamProduto() throws Exception {
        int databaseSizeBeforeUpdate = produtoRepository.findAll().size();
        produto.setId(count.incrementAndGet());

        // Create the Produto
        ProdutoDTO produtoDTO = produtoMapper.toDto(produto);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProdutoMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(produtoDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll();
        assertThat(produtoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteProduto() throws Exception {
        // Initialize the database
        produtoRepository.saveAndFlush(produto);

        int databaseSizeBeforeDelete = produtoRepository.findAll().size();

        // Delete the produto
        restProdutoMockMvc
            .perform(delete(ENTITY_API_URL_ID, produto.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Produto> produtoList = produtoRepository.findAll();
        assertThat(produtoList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
