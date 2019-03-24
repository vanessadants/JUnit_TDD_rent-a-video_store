package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.matchers.MatchersProprios.caiNumaSegunda;
import static br.ce.wcaquino.utils.DataUtils.isMesmaData;
import static br.ce.wcaquino.utils.DataUtils.obterDataComDiferencaDias;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoServiceTest {

	private LocacaoService service;
	
	@Rule
	public ErrorCollector error = new ErrorCollector();
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setup(){
		service = new LocacaoService();
	}
	
	@Test
	public void testeLocacao() throws Exception {
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		Filme filme1 = new Filme("Filme 1", 1, 5.0);
		Filme filme2 = new Filme("Filme 2", 2, 2.5);
		
		//acao
		Locacao locacao = service.alugarFilme(usuario, Arrays.asList(filme1, filme2));
			
		//verificacao
		error.checkThat(locacao.getValor(), is(equalTo(7.5)));
		error.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
		error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));
	}
	
	@Test(expected = FilmeSemEstoqueException.class)
	public void testLocacao_filmeSemEstoque() throws Exception{
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		Filme filme1 = new Filme("Filme 1", 1, 4.0);
		Filme filme2 = new Filme("Filme 2", 0, 6.0);
		
		//acao
		service.alugarFilme(usuario, Arrays.asList(filme1, filme2));
	}
	
	@Test
	public void testLocacao_usuarioVazio() throws FilmeSemEstoqueException{
		//cenario
		Filme filme1 = new Filme("Filme 1", 1, 4.0);
		Filme filme2 = new Filme("Filme 2", 1, 4.5);
		
		//acao
		try {
			service.alugarFilme(null, Arrays.asList(filme1,filme2));
			Assert.fail();
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usuario vazio"));
		}
	}

	@Test
	public void testLocacao_FilmeVazio() throws FilmeSemEstoqueException, LocadoraException{
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		
		exception.expect(LocadoraException.class);
		exception.expectMessage("Existe filme vazio");
		
		//acao
		service.alugarFilme(usuario, null);
	}
	
	@Test
	public void testeLocacaoComDescontoCrescente() throws Exception {
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		Filme filme1 = new Filme("Godzila", 1, 25d);
		Filme filme2 = new Filme("Pinoquio", 1, 20d);
		Filme filme3 = new Filme("O menino", 1, 30d);
		Filme filme4 = new Filme("Forrest", 1, 22d);
		Filme filme5 = new Filme("Ariel", 1, 28d);
		Filme filme6 = new Filme("High School", 1, 24d);
		
		//acao
		Locacao locacao = service.alugarFilme(usuario, Arrays.asList(filme1, filme2, filme3, filme4, filme5, filme6));
			
		//verificacao
		error.checkThat(locacao.getValor(), is(equalTo(85.5)));
		error.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
		error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));
	}
	
	@Test
	public void testeLocacaoMaiorQueLimite() throws Exception {
		//cenario
		exception.expect(LocadoraException.class);
		exception.expectMessage("O máximo de filmes permitidos para aluguel é 6.");
		
		Usuario usuario = new Usuario("Usuario 1");
		Filme filme1 = new Filme("Godzila", 1, 25d);
		Filme filme2 = new Filme("Pinoquio", 1, 20d);
		Filme filme3 = new Filme("O menino", 1, 30d);
		Filme filme4 = new Filme("Forrest", 1, 22d);
		Filme filme5 = new Filme("Ariel", 1, 28d);
		Filme filme6 = new Filme("High School", 1, 24d);
		Filme filme7 = new Filme("High School 2", 1, 24d);
		
		//acao
		service.alugarFilme(usuario, Arrays.asList(filme1, filme2, filme3, filme4, filme5, filme6, filme7));
	}
	
	@Test
	public void testeDevolverNaSegundaAoAlugarNoSabado() throws FilmeSemEstoqueException, LocadoraException{
		Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 1, 5.0));
		
		//acao
		Locacao retorno = service.alugarFilme(usuario, filmes);
		
		//verificacao
		assertThat(retorno.getDataRetorno(), caiNumaSegunda());
		
	}
	
}
