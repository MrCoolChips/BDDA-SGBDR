package bdda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Classe DiskManager
 * ------------------
 * Ce gestionnaire simule un système de gestion de pages sur disque.
 * Il permet d'allouer, désallouer, lire et écrire des pages, ainsi que
 * de sauvegarder/charger l'état des pages libres.
 */
public class DiskManager {
	
	private DBConfig config;
	// File (queue) qui contient les pages libres
	private Queue<PageId> freePages;

	public DiskManager(DBConfig config) {
		this.config = config;
		this.freePages = new LinkedList<>();
	}
	
	/**
	 * Alloue une nouvelle page.
	 * Si une page libre existe, on la réutilise.
	 * Sinon, on crée une nouvelle page dans un fichier.
	 */
	public PageId allocPage() throws IOException {

	    if (!freePages.isEmpty()) {
	    	// Réutilisation d'une page désallouée
	        return freePages.poll();
	    }

	    int fileIdx = 0;
	    
	    while (true) {
	        File f = new File(config.getPath(), "F" + fileIdx + ".rsdb");
	        if (!f.exists()) {
	            f.createNewFile();
	        }

	        if (f.length() + config.getPageSize() <= config.getMaxFileSize()) {
	            try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
	            	long pageIdx = raf.length() / config.getPageSize();
	                raf.seek(raf.length());
	                // page vide
	                raf.write(new byte[config.getPageSize()]);
	                return new PageId(fileIdx, (int) pageIdx);
	            }
	        }

	        fileIdx++;
	    }
	}
	
	/**
	 * Désalloue une page et l’ajoute dans la liste des pages libres.
	 */
	public void DeallocPage(PageId pageId) throws IOException {
		File f = getFile(pageId);
		// Vérifie que la page existe
		getOffset(pageId, f);
		freePages.add(pageId);
	}
	
	/**
	 * Lit une page à partir du disque et la charge dans un buffer.
	 */
	public void ReadPage(PageId pageId, byte[] buff) throws IOException {
		
		if (buff.length != config.getPageSize()) {
		    throw new IOException("Taille du buffer (" + buff.length + ") différente de la taille d'une page (" + config.getPageSize() + ")");
		}

	    File f = getFile(pageId);

	    try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
	    	long offset = getOffset(pageId, f);
	        raf.seek(offset);
	        raf.readFully(buff);
	    }
	}
	
	/**
	 * Écrit le contenu d'un buffer dans une page du disque.
	 */
	public void WritePage(PageId pageId, byte[] buff) throws IOException {
		
		if (buff.length != config.getPageSize()) {
		    throw new IOException("Taille du buffer (" + buff.length + ") différente de la taille d'une page (" + config.getPageSize() + ")");
		}

		File f = getFile(pageId);
		
		try(RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
	        long offset = getOffset(pageId, f);
	        raf.seek(offset);
	        raf.write(buff);
		}
	}

	/**
	 * Récupère le fichier correspondant à un identifiant de page.
	 */
	private File getFile(PageId pageId) throws IOException {
	    File f = new File(config.getPath(), "F" + pageId.getFileIdx() + ".rsdb");
	    
	    if (!f.exists()) {
	        throw new IOException("Fichier inexistant : " + f.getAbsolutePath());
	    }
	    
	    return f;
	}
	
	/**
	 * Calcule le décalage (offset) d'une page dans son fichier.
	 * Vérifie que la page existe bien.
	 */
	private long getOffset(PageId pageId, File f) throws IOException {
        long offset = (long) pageId.getPageIdx() * config.getPageSize();

        if (offset + config.getPageSize() > f.length()) {
            throw new IOException("Page " + pageId.getPageIdx() + 
            		" inexistante dans le fichier " + f.getName());
        }
        
        return offset;
	}
	
	/**
	 * Sauvegarde l'état des pages libres dans un fichier dm.save.
	 */
	public void SaveState() throws IOException {
	    File saveFile = new File(config.getPath(), "dm.save");
	    
	    try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
	        for (PageId page : freePages) {
	            writer.write(page.getFileIdx() + "," + page.getPageIdx());
	            writer.newLine();
	        }
	    }
	}
	
	/**
	 * Recharge l'état des pages libres depuis le fichier dm.save.
	 */
	public void LoadState() throws IOException {
	    File saveFile = new File(config.getPath(), "dm.save");
	    
	    freePages.clear();
	    
	    if (!saveFile.exists()) {
	    	// Aucun état sauvegardé
	        return;
	    }


	    try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
	        
	        String line;
	        while ((line = reader.readLine()) != null) {
	            
	            // Séparer chaque ligne en deux parties : fileIdx et pageIdx
	            String[] parts = line.split(",");
	            
	            // Vérifier que la ligne a bien deux parties
	            if (parts.length == 2) {
	                
	                // Convertir la première partie en entier : identifiant du fichier
	                int fileIdx = Integer.parseInt(parts[0].trim());
	                
	                // Convertir la deuxième partie en entier : index de la page dans le fichier
	                int pageIdx = Integer.parseInt(parts[1].trim());
	                
	                // Créer un PageId correspondant et l'ajouter à la liste des pages libres
	                freePages.add(new PageId(fileIdx, pageIdx));
	            }
	        }
	    }

	}
	
	//Pour voir les pages libre
	public void getFreePages() {
		Iterator<PageId> it = freePages.iterator();
		while(it.hasNext()) {
			System.out.println(it.next());
		}
	}

}
