package ca.bc.gov.nrs.wfprev.common.entities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import ca.bc.gov.nrs.wfone.common.service.api.model.factory.FactoryException;
import ca.bc.gov.nrs.wfone.common.utils.ByteUtils;

/**
 * Utility Class for generating an ETAG from an object
 */
public class ETag {
  private ETag() {}

  public static String generate(Object object) {
    String result = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] mdBytes = md.digest(getEntityToBytes(object));

			UUID uuid = ByteUtils.getUUID(mdBytes);

			result = uuid.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new FactoryException("Failed to create MD5 hash.", e);
		} catch (IOException e) {
			throw new FactoryException("Failed to serialize object.", e);
		}

		return result;
  }

  public static byte[] getEntityToBytes(Object object) throws IOException {

		byte[] result = null;

		ObjectOutput out = null;
		try(ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			
			out = new ObjectOutputStream(bos);
			out.writeObject(object);
			result = bos.toByteArray();

			out.close();
			out = null;

		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}

		return result;
	}
}
