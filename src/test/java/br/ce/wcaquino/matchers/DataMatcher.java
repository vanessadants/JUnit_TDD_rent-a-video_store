package br.ce.wcaquino.matchers;

import java.util.Date;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import br.ce.wcaquino.utils.DataUtils;

public class DataMatcher extends TypeSafeMatcher<Date> {

	private Date data;
	
	public DataMatcher(Date data) {
		this.data = data;
	}
	
	public void describeTo(Description desc) {
		desc.appendText(data.toString());
	}

	@Override
	protected boolean matchesSafely(Date data) {
		return DataUtils.isMesmaData(this.data, data);
	}

}
