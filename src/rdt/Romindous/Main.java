package rdt.Romindous;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.InputMismatchException;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Timer;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

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
		final JFileChooser jfc = new JFileChooser("C:/Users");
		
		final File dir;
		//System.out.println("[RE] >> Copy the RP root directory from the explorer e.g. '(dir)/assets'...");
		while (true) {
			final File fl = rqstFileDir(jfc, "Select the RP root directory...", "", true);
			System.out.println(fl);
			if (fl != null && chckIfExist(new File(fl.getAbsolutePath()  + "/pack.mcmeta"), false, false) && chckIfExist(new File(fl.getAbsolutePath() + "/assets/minecraft"), true, false)) {
				System.out.println("[RE] >> Accessed directory '" + fl.getName() + "'\n");
				dir = new File(fl.getAbsolutePath() + "/assets/minecraft");
				break;
			} else {
				System.out.println("[RE] >> The directory you entered isn't an RP root!\nTry entering one again...");
			}
		}
		
		System.out.println("[RE] >> Enter:\n(1) for <adding> a model\n(2) for <removing> a model\n(3) for <adding> a sound\nOr (4) to <remove> a sound\n(0) - exit");
		l : while (true) {
			//sc.next();
			try {
				final Scanner in = new Scanner(System.in);
				switch (in.nextInt()) {
				case 1:
					addModel(dir, in, jfc);
					break;
				case 2:
					delModel(dir, in, jfc);
					break;
				case 3:
					addSound(dir, in, jfc);
					break;
				case 4:
					delSound(dir, sc, jfc);
					break;
				case 0:
					System.out.println("\n[RE] >> Exiting script, bye xD");
					break l;
				default:
					in.close();
					throw new InputMismatchException();
				}
				System.out.println("\n[RE] >> Enter:\n(1) for <adding> a model\n(2) for <removing> a model\n(3) for <adding> a sound\nOr (4) to <remove> a sound\n(0) - exit");
			} catch (InputMismatchException e) {
				System.out.println("[ERROR] >> Pls enter a number 1-4...");
			}
		}
		sc.close();
		System.exit(0);
	}
	
	private static boolean addSound(final File dir, final Scanner sc, final JFileChooser jfc) {
		final Gson gs = new GsonBuilder().setPrettyPrinting().create();
		System.out.println("\n[RE] >> Enter the full name for the sounds,\nTo be stored under e.g. 'guns/p90/reload'...");
		final String snd = sc.next().replace('\\', '/').replace('.', '/');
		try {
			final File ss = new File(dir.getAbsolutePath() + "/sounds.json");
			System.out.println(ss.getAbsolutePath());
			final JsonObject jo = ss.createNewFile() ? new JsonObject() : JsonParser.parseReader(new JsonReader(new InputStreamReader(new FileInputStream(ss)))).getAsJsonObject();
			for (final Entry<String, JsonElement> en : jo.entrySet()) {
				if (en.getKey().replace('.', '/').equalsIgnoreCase(snd)) {
					System.out.println("\n[ERROR] >> The sound " + snd + " is already described in /sounds.json!");
					return false;
				}
			}

			final JsonObject so = new JsonObject();
			so.add("category", new JsonPrimitive("master"));
			System.out.println("\n[RE] >> Enter a subtitle or '0' for no sub...");
			final String sb = sc.next();
			so.add("subtitle", new JsonPrimitive(sb.length() == 1 ? "" : sb));
			final JsonArray ja = new JsonArray();
			System.out.println();
			while (true) {
				jfc.setSelectedFile(null);
				final File sf = rqstFileDir(jfc, "Select a sound file you want to add or exit to stop...", "ogg", false);
				if (sf == null) break;
				final String sPth = snd.substring(0, snd.lastIndexOf('/')) + "/" + sf.getName();
				final File nsf = new File(dir.getAbsolutePath() + "/sounds/" + sPth);
				nsf.getParentFile().mkdirs();
				if (nsf.createNewFile()) {
					Files.copy(sf.toPath(), nsf.toPath(), StandardCopyOption.REPLACE_EXISTING);
					System.out.println("[INFO] >> Sound file '" + nsf.getName() + "' created!");
				} else {
					System.out.println("[INFO] >> The sound '" + nsf.getName() + "' is already there!");
				}
				ja.add(new JsonPrimitive(sPth.substring(0, sPth.lastIndexOf('.'))));
			}
			
			if (ja.size() == 0) {
				System.out.println("\n[ERROR] >> No sounds were selected!");
				return false;
			}
			
			so.add("sounds", ja);
			jo.add(snd.replace('/', '.'), so);
			final OutputStreamWriter osr = new OutputStreamWriter(new FileOutputStream(ss));
			osr.write(gs.toJson(jo));
			osr.close();
			System.out.println("\n[INFO] >> The sound named '" + snd.replace('/', '.') + "' was created!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	private static boolean delSound(final File dir, final Scanner sc, final JFileChooser jfc) {
		final File ss = new File(dir.getAbsolutePath() + "/sounds.json");
		if (ss.exists()) {
			final Gson gs = new GsonBuilder().setPrettyPrinting().create();
			System.out.println("\n[RE] >> Enter the sound to be removed e.g. 'guns.ak47.reload'...");
			final String snd = sc.next();
			try {
				final JsonObject jo = JsonParser.parseReader(new JsonReader(new InputStreamReader(new FileInputStream(ss)))).getAsJsonObject();
				final JsonElement je = jo.remove(snd);
				if (je == null) {
					System.out.println("[ERROR] >> The sound '" + snd + "' is not assigned yet!");
					return false;
				} else {
					System.out.println("\n[RE] >> Remove sounds tied to the name (Y | N)?");
					if (sc.next().equalsIgnoreCase("Y")) {
						for (final JsonElement e : je.getAsJsonObject().getAsJsonArray("sounds")) {
							final Path pt = Path.of(dir.getAbsolutePath().replace('\\', '/') + "/sounds/" + e.getAsString() + ".ogg");
							if (Files.deleteIfExists(pt)) {
								System.out.println("[INFO] >> Deleting sound '" + pt.getFileName() + "' defined for " + snd);
								cleanDir(oneDownPth(pt), "sounds");
							}
						}
					}
				}
				
				final OutputStreamWriter osr = new OutputStreamWriter(new FileOutputStream(ss));
				osr.write(gs.toJson(jo));
				osr.close();
				System.out.println("\n[INFO] >> Sounds deleted for " + snd);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("[ERROR] >> No sounds are assigned yet!");
			return false;
		}
		return true;
	}

	private static boolean delModel(final File dir, final Scanner sc, final JFileChooser jfc) {
		jfc.setCurrentDirectory(dir);
		final File mdl = rqstFileDir(jfc, "Select the JSON model that you'd like to remove...", "json", false);
		if (mdl == null) {
			System.out.println("[ERROR] >> No selection was made, stopping removal.");
			return false;
		}
		System.out.println("\n[RE] >> Remove textures tied to the model (Y | N)?");
		return rmvMdl(dir, mdl, sc.next().equalsIgnoreCase("Y"));
	}

	private static boolean rmvMdl(final File dir, final File mdl, final boolean rmvTxs) {
		final Gson gs = new GsonBuilder().setPrettyPrinting().create();
		final File mrt = new File(dir.getAbsolutePath() + "/models/item");
		if (mrt.isDirectory()) {
			boolean ntFnd = true;
			try {
				if (rmvTxs) {
					System.out.println();
					final JsonObject jo = JsonParser.parseReader(new JsonReader(new InputStreamReader(new FileInputStream(mdl)))).getAsJsonObject();
					for (final Entry<String, JsonElement> en : jo.remove("textures").getAsJsonObject().entrySet()) {
						final File tx = new File(dir.getAbsoluteFile() + "/textures/" + en.getValue().getAsString() + ".png");
						if (tx.exists()) {
							System.out.println("[INFO] >> Deleting texture " + tx.getName() + " under the key " + en.getKey());
							final Path pt = tx.toPath();
							Files.delete(pt);
							cleanDir(oneDownPth(pt), "textures");
						} else {
							System.out.println("[INFO] >> Texture " + en.getValue().getAsString() + " under the key '" + en.getKey() + "' is not there, alr...");
						}
					}
					System.out.println("\n[INFO] >> All present textures were deleted!");
				}
				
				for (final File fl : mrt.listFiles()) {
					if (fl.isFile() && fl.getName().endsWith(".json")) {
						final JsonObject jo = JsonParser.parseReader(new JsonReader(new InputStreamReader(new FileInputStream(fl)))).getAsJsonObject();
						final JsonArray ja = jo.remove("overrides").getAsJsonArray();
						if (ja == null) continue;
						final String pth = mdl.getAbsolutePath();
						final JsonArray nja = new JsonArray(ja.size());
						for (final JsonElement o : ja) {
							if (pth.replace('\\', '/').endsWith(o.getAsJsonObject().get("model").getAsString() + ".json")) {
								System.out.println("\n[INFO] >> Found item " + fl.getName() + " to contain the model,\nWith CMD of " + o.getAsJsonObject().getAsJsonObject("predicate").get("custom_model_data").getAsInt() + ", removing --");
								ntFnd = false;
							} else {
								nja.add(o);
							}
						}
						
						if (nja.size() == 0) {
							System.out.println("\n[INFO] >> There are no more CMD references in this file, deleting it...");
							final Path pt = fl.toPath();
							Files.delete(pt);
							cleanDir(oneDownPth(pt), "models");
							continue;
						}
						jo.add("overrides", nja);
	
						final OutputStreamWriter osr = new OutputStreamWriter(new FileOutputStream(fl));
						osr.write(gs.toJson(jo));
						osr.close();
					}
				}

				System.out.println("\n[INFO] >> The model file '" + mdl.getAbsolutePath() + "' was deleted!");
				final Path pt = mdl.toPath();
				Files.delete(mdl.toPath());
				cleanDir(oneDownPth(pt), "models");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (ntFnd) {
				System.out.println("\n[INFO] >> The model " + mdl.getName() + " is not assigned to an item!");
			}
		} else {
			System.out.println("\n[INFO] >> The model " + mdl.getName() + " is not assigned to an item!");
		}
		return true;
	}

	private static boolean addModel(final File mcRoot, final Scanner sc, final JFileChooser jfc) {
		//System.out.println("[RE] >> Enter the JSON model path...");
		final File mdl = rqstFileDir(jfc, "Select the JSON model that you'd like to add...", "json", false);
		System.out.println(mdl);
		if (mdl == null) {
			System.out.println("[ERROR] >> No selection was made, stopping addition.");
			return false;
		}
		System.out.println("[RE] >> Enter a general directory for your model and textures.\nEnter the dir path e.g. 'guns/lmgs/negev'...");
		final String pth = sc.next().replace('\\', '/');
		final String mdlPth = pth + "/" + mdl.getName();
		System.out.println("[INFO] >> The file paths will be added to /models and /textures");
		System.out.println("\n[RE] >> What minecraft item should it be assigned to?");
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
			
			nmdl.getParentFile().mkdirs();
			nmdl.createNewFile();
			Files.copy(mdl.toPath(), nmdl.toPath(), StandardCopyOption.REPLACE_EXISTING);
			final JsonObject jo = JsonParser.parseReader(new JsonReader(new InputStreamReader(new FileInputStream(nmdl)))).getAsJsonObject();
			final JsonObject smp = jo.remove("textures").getAsJsonObject();
			for (final Entry<String, JsonElement> en : smp.entrySet()) {
				//System.out.println("\n[RE] >> Enter dir of a texture for '" + en.getKey() + "'...\nDir listed in file: " + en.getValue().getAsString());
				final File txr = rqstFileDir(jfc, "Enter dir of a texture for '" + en.getKey() + "'...\nDir listed in file: " + en.getValue(), "png", false);
				if (txr == null) {
					System.out.println("[ERROR] >> No selection was made, stopping addition.\nThe model constructed before will be deleted.");
					rmvMdl(mcRoot, nmdl, true);
					return false;
				}
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
			System.out.println("\n[INFO] >> The model file '" + nmdl.getAbsolutePath() + "' was created and textures loaded!");
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
	
	/*private static File scanFile(final Scanner sc, final boolean isDir, final boolean crt, final String ext) {
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
	}*/
	
	private static File rqstFileDir(final JFileChooser fc, final String name, final String ext, final boolean isDir) {
		fc.setDialogTitle(name);
		if (isDir) {
			fc.setFileSelectionMode(1);
		} else {
			fc.setFileFilter(new FileFilter() {
				@Override
				public String getDescription() {
					return "Files with extension ." + ext;
				}
				@Override
				public boolean accept(final File f) {
					return f.isDirectory() || f.getName().endsWith("." + ext);
				}
			});
			fc.setFileSelectionMode(0);
		}
		fc.showOpenDialog(null);
		final File fl = fc.getSelectedFile();
		if (fl != null) {
			fc.setCurrentDirectory(isDir ? fl : fl.getParentFile());
		}
		return fl != null && (isDir ? fl.isDirectory() : fl.getName().endsWith("." + ext)) ? fl : null;
	}
	
	private static boolean cleanDir(final Path pt, final String stopAt) {
		final File fl = pt.toFile();
		if (fl.isFile()) return false;
		if (fl.list().length == 0 && !pt.endsWith(stopAt) && !pt.endsWith(stopAt + "\\")) {
			try {
				Files.delete(pt);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return cleanDir(oneDownPth(pt), stopAt);
		}
		return true;
	}
	
	private static Path oneDownPth(final Path pt) {
		return Path.of(pt.toString().substring(0, pt.toString().lastIndexOf('\\')));
	}
}
