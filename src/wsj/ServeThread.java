/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wsj;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import wsj.HttpRequest.Key;

/**
 *
 * @author Will
 */
public class ServeThread extends Thread
{
	private Socket sock;

	private Boolean isRunning;

	private final Coordinator server;

	public ServeThread(Coordinator server)
	{
		this.setName(this.getName().replace("Thread", "Worker"));
		this.server = server;
		this.isRunning = true;
		
		this.start();
	}

	public synchronized void serve(Socket in)
	{
		if (sock != null) return;

		sock = in;

		this.notify();
	}

	public void halt()
	{
		isRunning = false;

		synchronized (this)
		{
			this.notify();
		}
	}

	@Override
	public void run()
	{
		server.registerThread(this);

		System.out.format(Coordinator.getString("out.start_thread"),
						Coordinator.getTimestamp(), this.getName());

		while (isRunning)
		{
			try
			{
				synchronized (this)
				{
					System.out.format(Coordinator.getString("out.waiting_thread"),
						Coordinator.getTimestamp(), this.getName());

					this.wait();

					System.out.format(Coordinator.getString("out.notified_thread"),
						Coordinator.getTimestamp(), this.getName());
				}

				if (sock != null)
				{
					serveRequests(sock);	// blocking method
	
					sock = null;

					server.pushServerQueue(this);
				}
			}
			catch (InterruptedException ex)
			{
				// Thread interrupted.
			}
		}

		System.out.format(Coordinator.getString("out.stop_thread"),
						Coordinator.getTimestamp(), this.getName());

		server.unregisterThread(this);
	}

	private void serveRequests(Socket sockOut)
	{
		BufferedOutputStream out = null;

		try
		{
			out = new BufferedOutputStream(sockOut.getOutputStream(), 4096);

			HttpRequest request = null;
			Integer timeout = 15;
			String[] temp;

			while (isRunning)
			{
				if (request != null)
				{
					try
					{
						timeout = Integer.parseInt(request.getValue(Key.KEEP_ALIVE));
					}
					catch (Exception ex)
					{
						timeout = 180;
					}

					timeout = 15;
				}

				temp = server.readRequest(sockOut, timeout);

				if (temp.length == 0) break;

				request = new HttpRequest(temp, sockOut.getInetAddress(), sockOut.getLocalPort());

				server.logAndPrintRequest(request);

				if (request.getValue(Key.METHOD).equals("GET"))
				{
					server.sendGetRequest(request, out);
				}
				else if (request.getValue(Key.METHOD).equals("HEAD"))
				{
					String buffer = Coordinator.unescapeURL(request.getValue(Key.RESOURCE));

					InputStream inBody = Coordinator.openResource(buffer);
					InputStream inHead = Coordinator.createHeader(200, buffer, inBody.available());
					Coordinator.sendResponseSilent(out, new InputStreamMixer(inHead, inBody));
				}
				else
				{
					String buffer = Coordinator.getString("pages.501");

					InputStream inBody = Coordinator.openResource(buffer);
					InputStream inHead = Coordinator.createHeader(501, buffer, inBody.available());
					Coordinator.sendResponseSilent(out, new InputStreamMixer(inHead, inBody));
				}

				out.flush();
			}
		}
		catch (HttpRequest.BadRequestException ex)
		{
			System.err.println(Coordinator.getString("err.invalid_request") + ex);
			server.logErr(ex);
		}
		catch (IOException ex)
		{
			System.err.println(Coordinator.getString("err.critical_fail") + ex);
			server.logErr(ex);
		}

		try
		{
			if (out != null) out.close();
			if (sockOut != null) sockOut.close();
		}
		catch (IOException ex)
		{
			System.err.println(Coordinator.getString("err.serve_close") + ex);
			server.logErr(ex);
		}
	}
}
