/*
import javax.crypto.*;
import javax.crypto.spec.*;

*/
import java.security.*;
import java.io.*;
import java.math.*;
import java.util.*;

public class rsa_funcs
{

	private static BufferedReader bufferedReader;

	/*
	 * Gets key file from command line with -k command
	 * This returns and array for the 3 values in the key file
	 */
	public static BigInteger[] key_file( String[] args ) throws Exception
	{
		BigInteger key_data[] = null;
				
		for ( int i = 0; i < args.length; i++ )
		{
			//-k <key file>:  required, specifies a file storing a valid AES key as a hex encoded string
			if ( args[i].equals("-k") )
			{
				i++;
				
				key_data =  read_integer_file(args[i]);

				break;
			}

		}

		if (key_data == null)
		{
			System.err.println("No file was found. Need to exit");
			System.exit(0);
		}
		
		return key_data;		
	}
	
	/*
	 * Gets key file from command line with -i command
	 * This returns the string of the value in the input file
	 */
	public static String input_file( String[] args ) throws Exception
	{
		String input_data = null;
				
		for ( int i = 0; i < args.length; i++ )
		{
			//-k <key file>:  required, specifies a file storing a valid AES key as a hex encoded string
			if ( args[i].equals("-i") )
			{
				i++;
				input_data = read_string_file(args[i]);
				break;
			}

		}

		if (input_data == null)
		{
			System.err.println("No file was found. Need to exit");
			System.exit(0);
		}
		
		return input_data;		
	}
	
	/*
	 * Gets output file name from command line with -o
	 */
	public static void output_file ( String[] args, String output ) throws Exception
	{
		for ( int i = 0; i < args.length; i++ )
		{
			//-o <output file>: required, specifies the path of the file where the resulting output is stored
			if ( args[i].equals("-o") )
			{
				i++;
				write_file( args[i], output );
				break;
			}
		}
	}

	/* Reads the data in a file, and converts the values into BigIntegers
	 * 
	 */
	public static BigInteger[] read_integer_file(String filename) throws Exception
	{
		
		BigInteger[] data = new BigInteger[3];

		
		try {
            File file = new File(filename);

            Scanner input = new Scanner(file);

            int i = 0;
            
            while (input.hasNextBigInteger())
            {
                BigInteger bi = input.nextBigInteger();
                data[i] = bi;
                i++;
            }
            
            input.close();
            
            return data;
		}
		catch (Exception e)
		{
			System.err.format("Exception occurred trying to read '%s'.", filename);
			e.printStackTrace();
			return null;
		}
	}
	
	
	/* Reads the information in a file and puts it into a single string
	 * 
	 */
	public static String read_string_file(String filename) throws Exception
	{
		
		
		try {
            FileReader file = new FileReader(filename);

            bufferedReader = new BufferedReader(file);
            
            String data = "";
            String line = null;
           
            while((line=bufferedReader.readLine())!=null)
            {
            	data += line;
            }
            		
            return data;
		}
		catch (Exception e)
		{
			System.err.format("Exception occurred trying to read '%s'.", filename);
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * Writes data to chosen file
	 */
	public static void write_file(String filename, String data) throws Exception
	{
		
		PrintWriter out = new PrintWriter(filename);
		try 
			{	
				out.print(data);
				
				out.close();
			}
			catch (Exception e)
			{
				System.err.format("Exception occurred trying to write '%s'.", filename);
				e.printStackTrace();
			}
			
	}
	
	
	public static BigInteger encrypt_data(BigInteger[] key_data, BigInteger message)
	{
		BigInteger rsa_number_of_bits = key_data[0];
		BigInteger rsa_N = key_data[1];
		BigInteger rsa_e = key_data[2];
		
		byte[] zero_byte = new byte[1];
		byte[] two_byte = new byte[1];
		byte[] random_bytes = create_random_bytes(rsa_number_of_bits);
		byte[] message_bytes = message.toByteArray();
		byte[] appended_bytes = null;
		BigInteger cipher_text;
		
		zero_byte[0] = (byte)0;
		two_byte[0] = (byte)2;

		/*
		formula for encryption:
			( 0x00 || 0x02 || r || 0x00 || m )^e mod N
		*/
		
		//This makes the ( 0x00 || 0x02 || r || 0x00 || m ) for encypting the message
		appended_bytes = append_byte_arrays(zero_byte, two_byte);
		appended_bytes = append_byte_arrays(appended_bytes, random_bytes);
		appended_bytes = append_byte_arrays(appended_bytes, zero_byte);
		appended_bytes = append_byte_arrays(appended_bytes, message_bytes);
		
		BigInteger int_msg = new BigInteger(appended_bytes);
		
		//This does ( int_msg )^e mod N 
	    cipher_text = int_msg.modPow(rsa_e, rsa_N);
		
		
		return cipher_text;
		
	}
	
	public static byte[] create_random_bytes(BigInteger num_bits)
	{
		byte[] random_bytes = null;

		
		boolean loop = true;
		
		while(loop)
		{
			SecureRandom random = new SecureRandom();

			BigInteger random_int = new BigInteger(num_bits.intValue(), random);
		
			random_bytes = random_int.toByteArray();
			
			loop = check_bytes_for_zero(random_bytes);
			
		}
		return random_bytes;
	}
	
	/*
	 * Makes sure there is no 0 byte in a byte array
	 */
	public static boolean check_bytes_for_zero(byte[] byte_array)
	{
		int i = 0;
		int array_size = byte_array.length;
		
		for(i = 0; i < array_size; i++ )
		{
			if( byte_array[i] == (byte)0 )
			{
				//there is a 0 in the array
				return true;
			}
		}
		
		//there was no 0 in the array
		return false;
	}

	
	
	/*
	 * Appends IV to cipher text message
	 */
	public static byte[] append_byte_arrays(byte[] original, byte[] data_to_add)
	{
	    byte[] result = new byte[original.length + data_to_add.length]; 
	    System.arraycopy(original, 0, result, 0, original.length); 
	    System.arraycopy(data_to_add, 0, result, original.length, data_to_add.length); 
	    return result;
	} 
	
	
	/* Just to print out the byte array
	 */
	public static void TestPrinting( byte[] data )
	{
		for(byte b:data)
		{
	        //
	        System.out.print(b);
	        System.out.print(" ");
	     }

		System.out.println();
	}	
}