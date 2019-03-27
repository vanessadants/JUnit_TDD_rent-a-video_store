package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.matchers.MatchersProprios.caiNumaSegunda;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHoje;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHojeComDiferencaDeDias;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

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
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LocacaoService.class})
@PowerMockRunnerDelegate()
public class LocacaoServiceTest {

	@InjectMocks
	private LocacaoService service;
	
	@Mock
	private SPCService spc;
	
	@Mock
	private LocacaoDAO dao;
	
	@Rule
	public ErrorCollector error = new ErrorCollector();
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
	}
	
	@Test(expected = FilmeSemEstoqueException.class)
	public void filmeSemEstoque() throws Exception{
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		Filme filme1 = new Filme("Filme 1", 1, 4.0);
		Filme filme2 = new Filme("Filme 2", 0, 6.0);
		
		//acao
		service.alugarFilme(usuario, Arrays.asList(filme1, filme2));
	}
	
	@Test
	public void usuarioVazio() throws FilmeSemEstoqueException{
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
	public void filmeVazio() throws FilmeSemEstoqueException, LocadoraException{
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		
		exception.expect(LocadoraException.class);
		exception.expectMessage("Existe filme vazio");
		
		//acao
		service.alugarFilme(usuario, null);
	}
	
	@Test
	public void locacaoComDescontoCrescente() throws Exception {
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
	}
	
	@Test
	public void locacaoMaiorQueLimite() throws Exception {
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
	public void locacaoDuranteSemanaDevolvendoDiaSeguinte() throws Exception {
		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		Filme filme1 = new Filme("Filme 1", 1, 5.0);
		Filme filme2 = new Filme("Filme 2", 2, 2.5);
		
		//acao
		Locacao locacao = service.alugarFilme(usuario, Arrays.asList(filme1, filme2));
			
		//verificacao
		error.checkThat(locacao.getValor(), is(equalTo(7.5)));
		error.checkThat(locacao.getDataLocacao(), ehHoje());
		error.checkThat(locacao.getDataRetorno(), ehHojeComDiferencaDeDias(1));
	}
	
	@Test
	public void locacaoDuranteSemanaDevolvendoDiaSeguintePowerMock() throws Exception {
		//cenario
		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(26, 3, 2019));
		
		Usuario usuario = new Usuario("Usuario 1");
		Filme filme1 = new Filme("Filme 1", 1, 5.0);
		Filme filme2 = new Filme("Filme 2", 2, 2.5);
		
		//acao
		Locacao locacao = service.alugarFilme(usuario, Arrays.asList(filme1, filme2));
			
		//verificacao
		error.checkThat(locacao.getValor(), is(equalTo(7.5)));
		error.checkThat(locacao.getDataLocacao(), is(new Date()));
		error.checkThat(locacao.getDataRetorno(), is(DataUtils.adicionarDias(new Date(),1)));
	}
	
	
	@Test
	public void locacaoSabadoDevolvendoSegunda() throws Exception{
		Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 1, 5.0));
		
		//acao
		Locacao locacao = service.alugarFilme(usuario, filmes);
		
		//verificacao
		error.checkThat(locacao.getValor(), is(equalTo(5.0)));
		error.checkThat(locacao.getDataLocacao(), ehHoje());
		error.checkThat(locacao.getDataRetorno(), caiNumaSegunda());
	}
	
	@Test
	public void locacaoSabadoDevolvendoSegundaPowerMock() throws Exception{
		//cenario
		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(30, 3, 2019));
		
		Date data= new Date();
		Usuario usuario = new Usuario("Usuario 1");
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 1, 5.0));
		
		//acao
		Locacao locacao = service.alugarFilme(usuario, filmes);
		
		//verificacao
		error.checkThat(locacao.getValor(), is(equalTo(5.0)));
		error.checkThat(locacao.getDataLocacao(), is(data));
		error.checkThat(locacao.getDataRetorno(), is(DataUtils.adicionarDias(data,2)));
	}
	
	@Test
	public void naoDeveAlugarFilmeParaNegativadoSPC() throws FilmeSemEstoqueException, LocadoraException{
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		Filme filme1 = new Filme("Filme 1", 1, 4.0);
		Filme filme2 = new Filme("Filme 2", 0, 6.0);
		
		when(spc.possuiNegativacao(Mockito.any(Usuario.class))).thenReturn(true);
		
		try{
			//acao
			service.alugarFilme(usuario, Arrays.asList(filme1,filme2));
			//verificacao
			Assert.fail();
		}catch(LocadoraException e) {
			Assert.assertThat(e.getMessage(),is("Usuário Negativado"));
		}
		
		Mockito.verify(spc).possuiNegativacao(usuario);
	}
	
	@Test
	public void deveProrrogarUmaLocacao() throws FilmeSemEstoqueException, LocadoraException {
		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		
		//cenario
		Date hoje = new Date();
		Locacao locacao = new Locacao();
		locacao.setUsuario(new Usuario("Usuario 1"));
		locacao.setFilmes(Arrays.asList(new Filme("Filme 1", 1, 5.0)));
		locacao.setDataLocacao(hoje);
		locacao.setDataRetorno(DataUtils.adicionarDias(hoje, 1));
		locacao.setValor(5.0);
		
		//acao
		service.prorrogarLocacao(locacao, 3);
		
		//verificacao
		ArgumentCaptor<Locacao> argCapt = ArgumentCaptor.forClass(Locacao.class);
		Mockito.verify(dao).salvar(argCapt.capture());
		Locacao locacaoRetornada = argCapt.getValue();
		
		error.checkThat(locacaoRetornada.getValor(), is(15.0));
		error.checkThat(locacaoRetornada.getDataLocacao(), ehHoje());
		error.checkThat(locacaoRetornada.getDataRetorno(), ehHojeComDiferencaDeDias(3));
	}
	
}
