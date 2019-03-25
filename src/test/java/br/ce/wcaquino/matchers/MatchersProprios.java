package br.ce.wcaquino.matchers;

import java.util.Calendar;
import java.util.Date;

import br.ce.wcaquino.utils.DataUtils;

public class MatchersProprios {

	public static DiaSemanaMatcher caiEm(Integer diaSemana) {
		return new DiaSemanaMatcher(diaSemana);
	}
	
	public static DiaSemanaMatcher caiNumaSegunda(){
		return new DiaSemanaMatcher(Calendar.MONDAY);
	}
	
	public static DataMatcher ehHoje(){
		return new DataMatcher(new Date());
	}
	
	public static DataMatcher ehHojeComDiferencaDeDias(int qntDias){
		return new DataMatcher(DataUtils.adicionarDias(new Date(), qntDias));
	}
	
}
