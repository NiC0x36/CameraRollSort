import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * This is a small application which cleans up the camera roll and sorts the
 * pictures according their file name into topic-specific folders
 * 
 * @DATE 2017-11-19
 * @author NiC0x36
 */
public class Sorter {
	// Directory where to take the pictures from
	// final static String SOURCE_DIRECTORY =

	final static String SOURCE_DIRECTORY = "E:/Clouds/OneDrive/Pictures/Eigene Aufnahmen";
	final static String SOURCE_DIRECTORY_TEST = "F:/Bilder/DUMP/TESTSOURCE";

	// Work directory
	final static String ROOT_WORK_DIRECTORY = "F:/Bilder/DUMP";
	final static String ROOT_WORK_DIRECTORY_TEST = "F:/Bilder/DUMP/TEST";

	// Directory to move the pictures first before sorting
	final static String UNSORTED_DIRECTORY = "F:/Bilder/DUMP/Unsorted";
	final static String UNSORTED_DIRECTORY_TEST = "F:/Bilder/DUMP/TEST/Unsorted";

	// Check here: http://myregexp.com/
	/*
	 * Some Facebook browser download names:
	 * 11224188_951859148186407_242622835125204321_o.jpg
	 * 13939339_10205146133771568_5189501550858674808_n.jpg
	 * 22007578_175903559650931_5357638589935853753_n.jpg
	 * 23213184_10214618612602495_8034071441328328736_o.jpg
	 * 22712633_183957315512222_7286920641784512858_o.jpg
	 */

	final static String[] DIRECTORIES_TO_SORT = { "(Snapchat-).*", "(FB_IMG_).*",
			"(^[0-9]{8}_)(([0-9]{15}_)|([0-9]{17}_)|([0-9]{16}_))(([0-9]{18}_(o|n))|([0-9]{19}_(o|n))).*", "(IMG_).*",
			"(Screenshot_).*", "(line_).*", "(^[12]\\d{3}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])_).*" };
	final static String UNSORTABLE_DIRECTORY = "UNDEF";

	public static void main(String[] args) throws InterruptedException {
		long time = System.currentTimeMillis();

		moveFiles(SOURCE_DIRECTORY, UNSORTED_DIRECTORY, false);

		System.out.println("\n Start sorting");
		long time1 = System.currentTimeMillis();
		moveFiles(UNSORTED_DIRECTORY, ROOT_WORK_DIRECTORY, true);

		System.out.println("\nTime to copy: " + (System.currentTimeMillis() - time) / 1000 + ","
				+ ((System.currentTimeMillis() - time) % 1000) + "s");

		System.out.println("Time to sort: " + (System.currentTimeMillis() - time1) / 1000 + ","
				+ ((System.currentTimeMillis() - time1) % 1000) + "s");
	}

	public static void moveFiles(String from, String to, Boolean sort) {
		System.out.println("Copying to: '" + to + "' ...");
		System.out.println("Sorting mode: " + sort + "\n");
		Path sourceDir = Paths.get(from);
		Path destinationDir = Paths.get(to);

		PrintDirectoryStreamContent(sourceDir);

		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(sourceDir)) {
			for (Path path : directoryStream) {
				Path destination = destinationDir.resolve(path.getFileName());
				System.out.println("\n\tmoving\t" + path);

				if (sort)
					destination = ObtainSortedDirectoryPath(to, path, destination);

				System.out.println("\tto \t" + destination);

				if (!Files.exists(destination)) {
					Files.createDirectories(destination.getParent());
					Files.createFile(destination);
				}
				Files.move(path, destination, REPLACE_EXISTING);

			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	// Decides where to put the file and returns the destination Directory
	private static Path ObtainSortedDirectoryPath(String to, Path path, Path destination) {
		Boolean found = false;
		for (String regex : DIRECTORIES_TO_SORT) {
			if (Pattern.matches(regex, path.getFileName().toString())) {
				System.out.println("\tPattern found\tcopy file...");

				destination = Paths.get(to + "/" + setDestinationDirectory(regex)).resolve(path.getFileName());
				found = true;
				break;
			}
		}

		if (!found) {
			destination = Paths.get(to + "/" + UNSORTABLE_DIRECTORY).resolve(path.getFileName());
			System.out.println("\tNo pattern found\tcopy file...");
		}
		return destination;
	}

	private static void PrintDirectoryStreamContent(Path sourceDir) {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(sourceDir)) {
			System.out.println("DIR STREAM:");
			for (Path path : directoryStream) {
				System.out.println(path.toString());
			}
			System.out.println("END");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Decides the destination folder of the picture
	private static String setDestinationDirectory(String regex) {
		String destinationDirectory;
		if (regex.equals(DIRECTORIES_TO_SORT[0]))
			destinationDirectory = "Snapchat";
		else if (regex.equals(DIRECTORIES_TO_SORT[1]) || regex.equals(DIRECTORIES_TO_SORT[2]))
			destinationDirectory = "Facebook";
		else if (regex.equals(DIRECTORIES_TO_SORT[3]))
			destinationDirectory = "IMG";
		else if (regex.equals(DIRECTORIES_TO_SORT[4]))
			destinationDirectory = "Screenshots";
		else if (regex.equals(DIRECTORIES_TO_SORT[5]))
			destinationDirectory = "line";
		else if (regex.equals(DIRECTORIES_TO_SORT[6]))
			destinationDirectory = "dcim";
		else
			destinationDirectory = UNSORTABLE_DIRECTORY;

		return destinationDirectory;
	}
}
