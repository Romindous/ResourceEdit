package rdt.Romindous;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Timer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {

	public static Timer tm;
	
	public static void main(final String[] args) {
		tm = new Timer();

		/*try {
			final InputStreamReader isr = new InputStreamReader(new FileInputStream(new File("bone.json")));
			final JSONObject jo = ((JSONObject) new JSONParser().parse(isr));
			final JSONArray ovs = (JSONArray) jo.get("overrides");
			for (final Object o : ovs) {
				if (((JSONObject) ((JSONObject) o).get("predicate")).containsValue(111l)) {
					System.out.println("[RE] >> This CustomModelData is already taken!");
				}
			}
			final JSONObject ovo = new JSONObject();
			final JSONObject prd = new JSONObject();
			prd.put("custom_model_data", 121);
			ovo.put("predicate", prd);
			ovo.put("model", "gun/ak47.json".substring(0, "gun/ak47.json".lastIndexOf('.')));
			ovs.add(ovo);
			jo.replace("overrides", ovs);
			
			final OutputStreamWriter osr = new OutputStreamWriter(new FileOutputStream(new File("stne.json")));
			osr.write(jo.toJSONString(). replace("\\", ""));
			osr.close();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}*/
		
		final Scanner sc = new Scanner(System.in);
		final File dir;
		System.out.println("[RE] >> Copy the RP directory from the explorer e.g. '(dir)/assets'...");
		while (true) {
			final File fl = new File(sc.next());
			if (fl.isDirectory() && chckIfExist(new File(fl.getAbsolutePath()  + "\\pack.mcmeta"), false, false) && chckIfExist(new File(fl.getAbsolutePath() + "\\assets\\minecraft"), true, false)) {
				System.out.println("[RE] >> Accessed directory '" + fl.getName() + "'\n");
				dir = new File(fl.getAbsolutePath() + "\\assets\\minecraft");
				break;
			} else {
				System.out.println("[RE] >> The directory you entered isn't a valid one, or is not an RP root!\nTry entering it again...");
			}
		}
		
		l : while (true) {
			//sc.next();
			System.out.println("[RE] >> Enter:\n(1) for <adding> a model\n(2) for <removing> a model\n(3) for <adding> a sound\nOr (4) to <remove> a sound\n(0) - exit");
			try {
				switch (sc.nextInt()) {
				case 1:
					addModel(dir, sc);
					break;
				case 2:
					//unfinished
					break;
				case 3:
					//unfinished
					break;
				case 4:
					//unfinished
					break;
				case 0:
					System.out.println("\n[RE] >> Exiting script, bye xD");
					break l;
				default:
					throw new InputMismatchException();
				}
			} catch (InputMismatchException e) {
				System.out.println("\nPls enter a number 1-4...");
				continue;
			}
		}
		sc.close();
		System.exit(0);
	}

	@SuppressWarnings("unchecked")
	private static boolean addModel(final File rpMct, final Scanner sc) {
		System.out.println("\n[RE] >> Enter the PNG model path...");
		final File img = scanFile(sc, false, false, ".png");
		System.out.println("[RE] >> And now the JSON model path...");
		final File mdl = scanFile(sc, false, false, ".json");
		System.out.println("[RE] >> What would you like the dir and model name to be?\nInclude the dir path e.g. 'guns\\ak47'...");
		final String pth = sc.next();
		System.out.println("[RE] >> The file paths will be added to \\models and \\");
		System.out.println("\n[RE] >> What item should it be assigned to?");
		final String itm = sc.next().toLowerCase().replace(' ', '_');
		System.out.println("[RE] >> And to what CustomModelData (int)?");
		int cmd;
		while (true) {
			try {
				cmd = sc.nextInt();
				break;
			} catch (InputMismatchException e) {
				System.out.println("[RE] >> CustomModelData must be an int...");
			}
		}
		
		if (new File(rpMct.getAbsolutePath() + "\\textures\\item\\" + pth).exists() || new File(rpMct.getAbsolutePath() + "\\models\\" + pth).exists()) {
			System.out.println("The model under the dir of '" + pth + "' already exists!");
			return false;
		}
		//new File(rpMct.getAbsolutePath() + "\\.json")
		final File itmDt = new File(rpMct.getAbsolutePath() + "\\models\\item\\" + itm + ".json");
		if (itmDt.exists()) {
			try {
				final InputStreamReader isr = new InputStreamReader(new FileInputStream(itmDt));
				final JSONObject jo = ((JSONObject) new JSONParser().parse(isr));
				final JSONArray ovs = (JSONArray) jo.get("overrides");
				for (final Object o : ovs) {
					if (((JSONObject) ((JSONObject) o).get("predicate")).containsValue(cmd)) {
						System.out.println("[RE] >> This CustomModelData is already taken!");
						return false;
					}
				}
				final JSONObject ovo = new JSONObject();
				final JSONObject prd = new JSONObject();
				prd.put("custom_model_data", cmd);
				ovo.put("predicate", prd);
				ovo.put("model", pth.substring(0, pth.lastIndexOf('.')));
				ovs.add(ovo);
				jo.replace("overrides", ovs);
				
				final OutputStreamWriter osr = new OutputStreamWriter(new FileOutputStream(itmDt));
				osr.write(jo.toJSONString(). replace("\\", ""));
				osr.close();
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
		} else {
			crtNewJSONDt(itmDt, itm, cmd, pth);
		}
		//unfinished
		return true;
	}

	private static void crtNewJSONDt(final File itmDt, final String itm, final int cmd, final String pth) {
		
	}

	private static boolean chckIfExist(final File pth, final boolean isDir, final boolean crtIfNot) {
		if (isDir) {
			if (pth.isDirectory()) {
				return true;
			} else if (crtIfNot) {
				pth.mkdirs();
				return true;
			}
		} else {
			if (pth.isFile()) {
				return true;
			} else if (crtIfNot) {
				try {
					pth.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		return false;
	}
	
	private static File scanFile(final Scanner sc, final boolean isDir, final boolean crt, final String ext) {
		while (true) {
			final File fl = new File(sc.next());
			if (chckIfExist(fl, isDir, crt)) {
				if (fl.getName().endsWith(ext)) {
					System.out.println("[RE] >> File " + fl.getName() + " is detecred");
					return fl;
				} else {
					System.out.println("[RE] >> The directory is not a '" + ext + "'! Try another");
				}
			} else {
				System.out.println("[RE] >> The directory is not valid! Try another...");
			}
		}
	}
}
