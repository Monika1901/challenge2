package aws.challenge;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 *
 * @author Monika
 */
public class AWSChallenge {

	private static final String METADATA_URL_FINAL = "http://169.254.169.254/latest/meta-data/";
	private static CloseableHttpClient httpClient = HttpClients.createDefault();

	public static void main(String[] args) {
		try {
			JSONObject metaData = AWSChallenge.getMetadata();
			JSONObject backupMetaData = metaData;
			String dataKey = "", dataKeyValue = "";
			if (args.length == 1) {
				dataKey = args[0];
				for (String key : dataKey.split("/")) {
					try {
						backupMetaData = backupMetaData.getJSONObject(key);
						dataKeyValue = backupMetaData.toString();
					} catch (Exception ex) {
						dataKeyValue = backupMetaData.getString(key);
					}
				}
				System.out.println(dataKeyValue.toString());
			} else {
				System.out.println(metaData.toString());
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private static JSONObject getMetadata() throws Exception {
		JSONObject JSON_METADATA = new JSONObject();
		AWSChallenge.getMetadataInJSON(AWSChallenge.METADATA_URL_FINAL, JSON_METADATA);
		httpClient.close();
		return JSON_METADATA;
	}

	private static void getMetadataInJSON(String METADATA_URL, JSONObject JM) throws Exception {
		String[] response = AWSChallenge.makeGetRequest(METADATA_URL);
		if (response[0].equals("404")) {
			return;
		}
		String[] response_array = response[1].split("\n");
		for (String key : response_array) {
			if (key.endsWith("/\n")) {
				continue;
			}
			String METADATA_URL_NEW = METADATA_URL + "" + key;
			if (key.endsWith("/")) {
				String newKey = key.substring(0, key.length() - 1);
				JM.put(newKey, new JSONObject());
				AWSChallenge.getMetadataInJSON(METADATA_URL_NEW, JM.getJSONObject(newKey));
			} else {
				String[] response_new = AWSChallenge.makeGetRequest(METADATA_URL_NEW);
				if (!response_new[0].equals("404")) {
					try {
						JM.put(key, new JSONObject(response_new[1]));
					} catch (Exception ex) {
						JM.put(key, response_new[1]);
					}
				} else {
					JM.put(key, new String());
				}
			}
		}
	}

	private static String[] makeGetRequest(String URL) throws Exception {
		HttpGet request = new HttpGet(URL);
		String[] result = { "", "" };
		CloseableHttpResponse response = httpClient.execute(request);
		result[0] = "" + response.getStatusLine().getStatusCode();
		try {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				result[1] = EntityUtils.toString(entity);
				return result;
			}
		} finally {
			response.close();
		}
		return null;
	}

}
