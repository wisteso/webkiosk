
package wsj;

/**
 *
 * @author Will
 */
public class Main
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
		try
		{
			Coordinator serve = new Coordinator(10, 8080, 80);

			serve.startServer();
		}
		catch (Exception ex)
		{
			System.err.println(ex);
		}
    }
}
