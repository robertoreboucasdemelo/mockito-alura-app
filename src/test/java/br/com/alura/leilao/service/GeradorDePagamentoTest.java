package br.com.alura.leilao.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.com.alura.leilao.dao.PagamentoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Pagamento;
import br.com.alura.leilao.model.Usuario;

class GeradorDePagamentoTest {
	
	private GeradorDePagamento gerador;
	
	@Mock
	private PagamentoDao pagamentoDao;
	
	@Mock
	private Clock clock;
	
	@Captor
	private ArgumentCaptor<Pagamento> captor;
	
	@BeforeEach
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		this.gerador = new GeradorDePagamento(pagamentoDao, clock);
	}
	
	private Leilao leilao(){
		
		Leilao leilao = new Leilao("Celular", new BigDecimal("500"),new Usuario("Fulano"));
		Lance lance = new Lance(new Usuario("Cicrano"),  new BigDecimal("900"));
		
		leilao.propoe(lance);
		leilao.setLanceVencedor(lance);
		return leilao;	
	}

	@Test
	void deveriaCriarPagamentoParaVencedorDoLeilaoNaSemana() {
		Leilao leilao = leilao();
		Lance vencedor = leilao.getLanceVencedor();
		
		LocalDate data = LocalDate.of(2022, 3, 2);	
		Instant instant = data.atStartOfDay(ZoneId.systemDefault()).toInstant();		
		Mockito.when(clock.instant()).thenReturn(instant);
		Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());
		
		gerador.gerarPagamento(vencedor);
		
		Mockito.verify(pagamentoDao).salvar(captor.capture());
		
		Pagamento pagamento = captor.getValue();
		Assert.assertEquals(LocalDate.now().plusDays(1), pagamento.getVencimento());
		Assert.assertEquals(vencedor.getValor(), pagamento.getValor());
		Assert.assertFalse(pagamento.getPago());
		Assert.assertEquals(vencedor.getUsuario(), pagamento.getUsuario());
		Assert.assertEquals(leilao, pagamento.getLeilao());
	}
	
	@Test
	void deveriaCriarPagamentoParaVencedorDoLeilaoNoSabado() {
		Leilao leilao = leilao();
		Lance vencedor = leilao.getLanceVencedor();
		LocalDate dataEsperada = LocalDate.of(2022, 3, 7);
		
		LocalDate data = LocalDate.of(2022, 3, 4);	
		Instant instant = data.atStartOfDay(ZoneId.systemDefault()).toInstant();		
		Mockito.when(clock.instant()).thenReturn(instant);
		Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());
		
		gerador.gerarPagamento(vencedor);
		
		Mockito.verify(pagamentoDao).salvar(captor.capture());
		
		Pagamento pagamento = captor.getValue();
		Assert.assertEquals(dataEsperada, pagamento.getVencimento());
		Assert.assertEquals(vencedor.getValor(), pagamento.getValor());
		Assert.assertFalse(pagamento.getPago());
		Assert.assertEquals(vencedor.getUsuario(), pagamento.getUsuario());
		Assert.assertEquals(leilao, pagamento.getLeilao());
	}
	
	@Test
	void deveriaCriarPagamentoParaVencedorDoLeilaoNoDomingo() {
		Leilao leilao = leilao();
		Lance vencedor = leilao.getLanceVencedor();
		LocalDate dataEsperada = LocalDate.of(2022, 3, 7);
		
		LocalDate data = LocalDate.of(2022, 3, 5);	
		Instant instant = data.atStartOfDay(ZoneId.systemDefault()).toInstant();		
		Mockito.when(clock.instant()).thenReturn(instant);
		Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());
		
		gerador.gerarPagamento(vencedor);
		
		Mockito.verify(pagamentoDao).salvar(captor.capture());
		
		Pagamento pagamento = captor.getValue();
		Assert.assertEquals(dataEsperada, pagamento.getVencimento());
		Assert.assertEquals(vencedor.getValor(), pagamento.getValor());
		Assert.assertFalse(pagamento.getPago());
		Assert.assertEquals(vencedor.getUsuario(), pagamento.getUsuario());
		Assert.assertEquals(leilao, pagamento.getLeilao());
	}

}
