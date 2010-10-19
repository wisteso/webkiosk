/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ftp;

/**
 *
 * @author wsoderbe
 */
public class FtpRunnerMain
{
	public static void main(String[] args) throws Exception
	{
		ListenThread lt = new ListenThread(21);
		lt.startServer();
	}
}
