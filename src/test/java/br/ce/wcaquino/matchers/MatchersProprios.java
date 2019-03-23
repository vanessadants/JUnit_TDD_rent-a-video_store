package br.ce.wcaquino.matchers;

import java.time.LocalDate;
import java.util.Calendar;

public class MatchersProprios {

	public static DiaSemanaMatcher caiEm(Integer diaSemana) {
		return new DiaSemanaMatcher(diaSemana);
	}
	
	public static DiaSemanaMatcher caiNumaSegunda(){
		return new DiaSemanaMatcher(Calendar.MONDAY);
	}
	
	public static DiaSemanaMatcher ehHoje(){
		return new DiaSemanaMatcher(Calendar.SUNDAY + LocalDate.now().getDayOfWeek().getValue());
	}
	
	public static DiaSemanaMatcher ehHojeComDiferencaDeDias(int qntDias){
		return new DiaSemanaMatcher(Calendar.SUNDAY + LocalDate.now().getDayOfWeek().plus(qntDias).getValue());
	}
	
}
