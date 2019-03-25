package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoService {
	
	private LocacaoDAO dao;
	private SPCService spcService;
	
	public Locacao alugarFilme(Usuario usuario, List<Filme> filmes) throws FilmeSemEstoqueException, LocadoraException {
		if(usuario == null) {
			throw new LocadoraException("Usuario vazio");
		}
		
		if(spcService.possuiNegativacao(usuario)) {
			throw new LocadoraException("Usuário Negativado");
		}
		
		if(filmes == null || filmes.isEmpty() || filmes.stream().anyMatch(Objects::isNull)) {
			throw new LocadoraException("Existe filme vazio");
		}
		
		if(filmes.size() > 6) {
			throw new LocadoraException("O máximo de filmes permitidos para aluguel é 6.");
		}
		
		if(filmes.stream().anyMatch(f -> f.getEstoque() == 0)) {
			throw new FilmeSemEstoqueException();
		}
		
		Locacao locacao = new Locacao();
		locacao.setFilmes(filmes);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(new Date());
		locacao.setValor(getValorTotalAluguel(filmes));

		//Entrega no dia seguinte
		Date dataEntrega = new Date();
		dataEntrega = adicionarDias(dataEntrega, 1);
		if(DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY)) {
			dataEntrega = adicionarDias(dataEntrega, 1);
		}
		locacao.setDataRetorno(dataEntrega);
		
		//Salvando a locacao...	
		dao.salvar(locacao);
		
		return locacao;
	}

	private double getValorTotalAluguel(List<Filme> filmes) {
		double total=0;
		for (int i = 0; i < filmes.size(); i++) {
			total += filmes.get(i).getPrecoLocacao()*getPercentualPorFilme(i+1);
		}
		return total;
	}

	private Double getPercentualPorFilme(int numeroFilme) {
		double percentual = 1;
		switch (numeroFilme) {
			case 3:
				percentual = 0.75;
				break;
			case 4:
				percentual = 0.5;
				break;
			case 5:
				percentual = 0.25;
				break;
			case 6:
				percentual = 0;
				break;
			default:
				break;
			}
		return percentual;
	}
	
	public void prorrogarLocacao(Locacao locacao, int dias) {
		Locacao novaLocacao = new Locacao();
		novaLocacao.setUsuario(locacao.getUsuario());
		novaLocacao.setFilmes(locacao.getFilmes());
		novaLocacao.setDataLocacao(new Date());
		novaLocacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(dias));
		novaLocacao.setValor(locacao.getValor() * dias);
		dao.salvar(novaLocacao);
	}
	
}