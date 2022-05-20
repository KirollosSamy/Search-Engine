package apt.project.Indexer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tartarus.snowball.ext.PorterStemmer;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class Parser_ {
	private String URL;
	// private String[] Result;

	private String[] head;
	private String[] h1;
	private String[] h2;
	private String[] h3;
	private String[] h4;
	private String[] body;
	private String[] footer;
	private String[] div;
	private String[] title;

	private boolean isParsed;
	Document doc;

	public Parser_(String url) {
		this.URL = url;
		this.isParsed = false;
	}

	public String[] getCopyOfText(String imp) { // head
		if (this.isParsed == false) {
			return null;
		}

		switch (imp) {
			case "head":
				return this.head;

			case "h1":
				return this.h1;

			case "h2":
				return this.h2;

			case "h3":
				return this.h3;

			case "h4":
				return this.h4;

			case "body":
				return this.body;

			case "footer":
				return this.footer;

			case "div":
				return this.div;

			case "title":
				return this.title;

		}

		return this.head;
	}

	public void setURL(String url) {
		this.URL = url;
	}

	public void htmlToTextParse() throws IOException {
		String[] tags = { "p", "" };
		doc = Jsoup.connect(this.URL).get();
		// String[] myText = doc.select("head").text().split(" "); //head h1 h2 h3 h4
		// body footer div title

		this.head = doc.select("head").text().split(" ");
		this.h1 = doc.select("h1").text().split(" ");
		this.h2 = doc.select("h2").text().split(" ");
		this.h3 = doc.select("h3").text().split(" ");
		this.h4 = doc.select("h4").text().split(" ");
		this.body = doc.select("body").text().split(" ");
		this.footer = doc.select("footer").text().split(" ");
		this.div = doc.select("title").text().split(" ");

		this.isParsed = true;

	}

	private List<String> RemoveStopWords(List<String> words, String stop_words_file_path)
			throws FileNotFoundException, IOException {
		HashSet<String> stops = new HashSet<>(); // initilalizing set
		File file = new File(stop_words_file_path);// send path here will be changed offcourse
													// D:\\GITHUB\\Search-Engine\\SearchEngine\\Indexer\\src\\indexer\\stopwords.txt
		BufferedReader reader = new BufferedReader(new FileReader(file));

		String word_;
		while ((word_ = reader.readLine()) != null) {

			stops.add(word_);
		}
		reader.close();

		List<String> list = new ArrayList<String>();
		for (String word : words) {
			if (!stops.contains(word) /* && !stops.contains(word+".") && stops.contains(word+":") */) {
				list.add(word);
			}
		}
		return list;
	}

	public List<String> Removing_Unwanted_Symbols(String[] withSymbols) {
		List<String> list = new ArrayList<String>();

		StringBuilder newWord = new StringBuilder();
		for (String word : withSymbols) {
			for (int i = 0; i < word.length(); i++) {
				char x = word.charAt(i);
				if ((x >= 'a' && x <= 'z') || (x >= 'A' && x <= 'Z') || (x >= '0' && x <= '9')) {
					int index = i;
					newWord.setLength(0);
					char x2 = word.charAt(index);
					while ((x2 >= 'a' && x2 <= 'z') || (x2 >= 'A' && x2 <= 'Z') || (x2 >= '0' && x2 <= '9')) {
						newWord.append(x2); // append new character to new word iam constructing
						index++; // go to the next character
						if (index < word.length())
							x2 = word.charAt(index);
						else
							break;
						/*
						 * [*] The idea of looping again is that assume string is 21/5/2001
						 * here i want to split the word into three words (21) (5) (2001)
						 * 
						 * [*] Another example : when i was working on links at wekepidia i noticed
						 * words like :(faka-Tonga)
						 * if i removed the (-) only i will change the meaining (fakaTonga) so i should
						 * split it into two words (faka)+(tonga) i dont really know the meaining of
						 * this shit but take it as example :D
						 * 
						 * .khaled.
						 */
					}
					i = index - 1;
					list.add(newWord.toString().toLowerCase());
				}
			}

		}
		return list;
	}

	private String[] getStemmedWords(List<String> without_stemming) {
		String[] stemmedWords = new String[without_stemming.size()];
		PorterStemmer stemmer = new PorterStemmer();

		for (int i = 0; i < without_stemming.size(); i++) {
			stemmer.setCurrent(without_stemming.get(i));
			stemmer.stem();
			String stemmo = stemmer.getCurrent();
			stemmedWords[i] = stemmo;
		}
		return stemmedWords;
	}

	public String[] processData(String[] data) throws IOException {
		List<String> without_stop_words = new ArrayList<String>();
		String[] final_stage = null;

		List<String> without_unwanted_symbols = Removing_Unwanted_Symbols(data);
		if (without_unwanted_symbols.size() != 0) {
			without_stop_words = RemoveStopWords(without_unwanted_symbols,
					"D:\\GITHUB\\Search-Engine\\SearchEngine\\src\\stopwords.txt");
			if (without_stop_words.size() != 0)
				final_stage = getStemmedWords(without_stop_words);
		}
		return final_stage;
	}

}