import java.util.*;
import java.lang.*;
import java.io.*;

/* Name of the class has to be "Main" only if the class is public. */
class Ideone
{
	public static void main (String[] args) throws java.lang.Exception
	{
		String file ="main.log";
        long first_kill_time = 2000000000000L;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            while (true) {
                // System.out.println(reader.readLine());
                String currentLine = reader.readLine();
                if (currentLine == null) break;
                if (currentLine.contains("Kill node")) 
                {
                    String _temp_time = "";
                    for (int i = currentLine.length()-13; i < currentLine.length(); i++) {
                        _temp_time += currentLine.charAt(i);
                    }
                    first_kill_time = Long.parseLong(_temp_time);
                    break;
                }
            }
        // } catch (NumberFormatException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(first_kill_time);
	}

    
}