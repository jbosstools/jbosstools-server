import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RunApp {

	public static void main(String[] args) throws IOException {
		System.out.println("Running dummy java app");
		BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));
		String name = reader.readLine();
	}

}
