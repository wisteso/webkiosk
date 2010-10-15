package wsj;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Will
 */
public class HttpRequest
{
    public InetAddress source;

    public Integer port;

    public String[] requestArray;

    private Map<String, String> mapping;

    public HttpRequest(String[] request, InetAddress source, Integer port) throws BadRequestException
    {
		try
		{
			String[] temp = request[0].split(" ");

			if (temp.length != 3) throw new Exception();

			mapping = new HashMap<String, String>();
			mapping.put("METHOD", temp[0].trim().toUpperCase());
			mapping.put("RESOURCE", temp[1].trim());
			mapping.put("VERSION", temp[2].trim().toUpperCase());

			for (int i = 1; i < request.length; ++i)
			{
				addValue(request[i]);
			}

			this.port = port;
			this.source = source;
			this.requestArray = request.clone();
		}
		catch (Exception ex)
		{
			throw new BadRequestException(ex);
		}
    }

    public void addValue(String input) throws Exception
    {
		String value = input.substring(input.indexOf(":") + 1).trim();

		String key = input.substring(0, input.indexOf(":")).trim();

		key = key.trim().toUpperCase().replaceAll("[^A-Z]", "_");

		mapping.put(key, value);
    }

    public String getValue(Key field)
    {
		return mapping.get(field.toString());
    }

    public enum Key
    {
		METHOD, RESOURCE, VERSION, HOST, USER_AGENT, ACCEPT, ACCEPT_LANGUAGE,
		ACCEPT_ENCODING, ACCEPT_CHARSET, KEEP_ALIVE, CONNECTION, REFERER
    }

    public static class BadRequestException extends Exception
    {
		public BadRequestException()
		{
			super();
		}

		public BadRequestException(String message)
		{
			super(message);
		}

		public BadRequestException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public BadRequestException(Throwable cause)
		{
			super(cause);
		}
    }
}