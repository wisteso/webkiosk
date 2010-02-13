package wsj;

import java.io.*;

/**
 *
 * @author Will
 */
public class InputStreamMixer extends InputStream
{
    private InputStream[] input;

    private int current;

    public InputStreamMixer(InputStream... arr)
    {
		input = arr;
    }

    @Override
    public int read() throws IOException
    {
		if (input[current].available() > 0)
		{
			return input[current].read();
		}
		else if (current < input.length - 1)
		{
			current++;

			return read();
		}

		return -1;
    }

    @Override
    public int available() throws IOException
    {
		int count = 0;

		for (int i = current; i < input.length; ++i)
		{
			count += input[i].available();
		}

		return count;
    }

    @Override
    public void close() throws IOException
    {
		for (int i = current; i < input.length; ++i)
		{
			input[i].close();
		}
    }
}
