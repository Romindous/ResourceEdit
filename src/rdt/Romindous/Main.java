package rdt.Romindous;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.InputMismatchException;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Timer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

public class Main {

	public static Timer tm;
	
	public static void main(final String[] args) {
		tm = new Timer();
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
		
		System.out.println("[RE] >> Enter:\n(1) for <adding> a model\n(2) for <removing> a model\n(3) for <adding> a sound\nOr (4) to <remove> a sound\n(0) - exit");
		l : while (true) {
			//sc.next();
			try {
				final Scanner in = new Scanner(System.in);
				switch (in.nextInt()) {
				case 1:
					addModel(dir, in);
					System.out.println("\n[RE] >> Enter:\n(1) for <adding> a model\n(2) for <removing> a model\n(3) for <adding> a sound\nOr (4) to <remove> a sound\n(0) - exit");
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
					in.close();
					throw new InputMismatchException();
				}
				continue;
			} catch (InputMismatchException e) {
				System.out.println("[ERROR] >> Pls enter a number 1-4...");
				continue;
			}
		}
		sc.close();
		System.exit(0);
	}
	
	private static boolean addModel(final File mcRoot, final Scanner sc) {
		System.out.println("[RE] >> Enter the JSON model path...");
		final File mdl = scanFile(sc, false, false, ".json");
		System.out.println("[RE] >> Choose a general directory for your model and textures.\nEnter the dir path e.g. 'guns/lmgs'...");
		final String pth = sc.next();
		final String mdlPth = pth + "/" + mdl.getName();
		System.out.println("[INFO] >> The file paths will be added to //models and /textures");
		System.out.println("\n[RE] >> What item should it be assigned to?");
		final String itm = sc.next().toLowerCase().replace(' ', '_');
		System.out.println("[RE] >> And to what CustomModelData (int)?");
		int cmd;
		while (true) {
			try {
				cmd = sc.nextInt();
				break;
			} catch (InputMismatchException e) {
				System.out.println("[ERROR] >> CustomModelData must be an int...");
			}
		}
		
		final File nmdl = new File(mcRoot.getAbsolutePath() + "/models/" + pth + "/" + mdl.getName());
		if (nmdl.exists()) {
			System.out.println("The model under the dir of '" + pth + "' already exists!");
			return false;
		}
		//new File(rpMct.getAbsolutePath() + "\\.json")
		final Gson gs = new GsonBuilder().setPrettyPrinting().create();
		final File itmDt = new File(mcRoot.getAbsolutePath() + "/models/item/" + itm + ".json");
		itmDt.getParentFile().mkdirs();
		try {
			if (itmDt.createNewFile()) {
				crtNewJSONDt(itmDt, itm, cmd, mdlPth, gs);
			} else {
				final JsonObject jo = JsonParser.parseReader(new JsonReader(new InputStreamReader(new FileInputStream(itmDt)))).getAsJsonObject();
				final JsonArray ja = jo.remove("overrides").getAsJsonArray();
				for (final JsonElement o : ja) {
					if (o.getAsJsonObject().getAsJsonObject("predicate").get("custom_model_data").getAsInt() == cmd) {
						System.out.println("[ERROR] >> This CustomModelData is already taken!");
						return false;
					}
				}
				final JsonObject prd = new JsonObject();
				prd.add("custom_model_data", new JsonPrimitive(cmd));
				final JsonObject ovo = new JsonObject();
				ovo.add("predicate", prd);
				ovo.add("model", new JsonPrimitive(mdlPth.substring(0, mdlPth.lastIndexOf('.'))));
				ja.add(ovo);
				jo.add("overrides", ja);

				final OutputStreamWriter osr = new OutputStreamWriter(new FileOutputStream(itmDt));
				osr.write(gs.toJson(jo));
				osr.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		nmdl.getParentFile().mkdirs();
		try {
			nmdl.createNewFile();
			Files.copy(mdl.toPath(), nmdl.toPath(), StandardCopyOption.REPLACE_EXISTING);
			final JsonObject jo = JsonParser.parseReader(new JsonReader(new InputStreamReader(new FileInputStream(nmdl)))).getAsJsonObject();
			final JsonObject smp = jo.remove("textures").getAsJsonObject();
			for (final Entry<String, JsonElement> en : smp.entrySet()) {
				System.out.println("\n[RE] >> Enter dir of a texture for '" + en.getKey() + "'...\nDir listed in file: " + en.getValue().getAsString());
				final File txr = scanFile(sc, false, false, ".png");
				final File nw = new File(mcRoot.getAbsolutePath() + "/textures/" + pth + "/" + txr.getName());
				nw.getParentFile().mkdirs();
				if (nw.createNewFile()) {
					Files.copy(txr.toPath(), nw.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} else {
					System.out.println("[INFO] >> The file '" + nw.getName() + "' already exists!");
				}
				en.setValue(new JsonPrimitive((pth + "/" + txr.getName()).substring(0, (pth + "/" + txr.getName()).lastIndexOf('.')).replace('\\', '/')));
			}
			jo.add("textures", smp);
			
			final OutputStreamWriter osr = new OutputStreamWriter(new FileOutputStream(nmdl));
			osr.write(gs.toJson(jo));
			osr.close();
			return true;
		} catch (JsonIOException | JsonSyntaxException | IOException ex) {
			ex.printStackTrace();
		}
		//Files.move(Path., null, null)
		return true;
	}

	private static void crtNewJSONDt(final File itmDt, final String itm, final int cmd, final String pth, final Gson gs) throws IOException {
		final JsonObject jo = new JsonObject();
		jo.add("parent", new JsonPrimitive("item/handheld"));
		final JsonObject tx = new JsonObject();
		tx.add("layer0", new JsonPrimitive(itm));
		jo.add("texture", tx);
		final JsonArray ovs = new JsonArray(1);
		final JsonObject prd = new JsonObject();
		prd.add("custom_model_data", new JsonPrimitive(cmd));
		final JsonObject ovo = new JsonObject();
		ovo.add("predicate", prd);
		ovo.add("model", new JsonPrimitive(pth.substring(0, pth.lastIndexOf('.'))));
		ovs.add(ovo);
		jo.add("overrides", ovs);

		final OutputStreamWriter osr = new OutputStreamWriter(new FileOutputStream(itmDt));
		osr.write(gs.toJson(jo));
		osr.close();
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
					System.out.println("[INFO] >> File " + fl.getName() + " is detected");
					return fl;
				} else {
					System.out.println("[ERROR] >> The directory is not a '" + ext + "'! Try another");
				}
			} else {
				System.out.println("[ERROR] >> The directory is not valid! Try another...");
			}
		}
	}
}
