package ca.bc.gov.nrs.wfprev.common.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Example of a converter for attributes
 */

@Converter
public class BooleanConverter implements AttributeConverter<Boolean, String> {

	/**
	 * Convert Boolean object to a String
	 * Stored in DB as VARCHAR(1) 'Y' or 'N'
	 */
	@Override
	public String convertToDatabaseColumn(Boolean convert) {
		if (convert != null) {
      return convert ? "Y" : "N";
		}
		return null;
	}

	@Override
	public Boolean convertToEntityAttribute(String convert) {
		if (convert != null) {
			return convert.equalsIgnoreCase("Y");
		}
		return null;
	}
}
