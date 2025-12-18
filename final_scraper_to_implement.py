import requests
from bs4 import BeautifulSoup, NavigableString
import re
import sys

def final_scrape(url):
    """
    Final robust scraper for karnatik.com, using a DOM traversal method
    to be resilient to messy HTML.
    """
    try:
        print(f"Fetching URL: {url}...")
        response = requests.get(url, headers={'User-Agent': 'Mozilla/5.0'})
        response.raise_for_status()
        doc = BeautifulSoup(response.text, 'html.parser')
        print("URL fetched successfully.")

        # The most reliable container is the main right-hand TD
        main_content_cell = doc.find('td', attrs={'width': '100%', 'valign': 'top'})
        if not main_content_cell:
            print("ERROR: Could not find main content TD block.")
            return
        
        # --- Title & Metadata from the cell's text ---
        cell_text = main_content_cell.get_text(" ", strip=True)
        title = "Unknown Title"
        title_match = re.search(r"Song:\s*(.*?)(?:raagam:|Aa:)", cell_text, re.DOTALL | re.IGNORECASE)
        if title_match:
            # Handle duplicate title issue by finding unique words
            title_parts = title_match.group(1).strip().split()
            title = " ".join(dict.fromkeys(title_parts))

        raga_match = re.search(r"raagam:\s*([\w\s\d]+?)\s*(?:Aa:|taaLam:)", cell_text, re.I)
        raga = raga_match.group(1).strip() if raga_match else "Unknown"
        tala_match = re.search(r"taaLam:\s*([\w\s]+?)\s*Composer:", cell_text, re.I)
        tala = tala_match.group(1).strip() if tala_match else "Unknown"
        composer_match = re.search(r"Composer:\s*([\w\s]+?)\s*Language:", cell_text, re.I)
        composer = composer_match.group(1).strip() if composer_match else "Unknown"

        # --- Lyrics Parsing via DOM Traversal ---
        lyrics_lines = []
        
        # 1. Find the starting anchor element (the <hr> tag after metadata)
        start_anchor = main_content_cell.find(lambda tag: 'Language:' in tag.get_text())
        if start_anchor:
            start_hr = start_anchor.find_next('hr')
            if start_hr:
                # 2. Iterate through all elements after the starting <hr>
                for element in start_hr.find_next_siblings():
                    # 3. Stop when we hit the "Meaning" section
                    if element.find('b', string=re.compile("Meaning", re.I)):
                        break
                    
                    # Ignore non-lyric elements
                    if element.name == 'hr' or element.name == 'center':
                        continue
                    
                    # Handle section headers
                    text = element.get_text(strip=True)
                    if text.lower() in ['pallavi', 'anupallavi', 'charanam', 'caranam']:
                        lyrics_lines.append(f"\n[{text.capitalize()}]")
                    elif text:
                        # For lyric lines, preserve line breaks from <br> tags
                        content_with_brs = str(element).replace('<br>', '\n').replace('<br/>', '\n')
                        cleaned_text = BeautifulSoup(content_with_brs, 'html.parser').get_text('\n', strip=True)
                        lyrics_lines.append(cleaned_text)

        lyrics = "\n".join(lyrics_lines).strip()
        # Clean up excessive newlines that might form
        lyrics = re.sub(r'\n\s*\n\s*\n+', '\n\n', lyrics).strip()
        
        # --- Print Results ---
        print("\n===========================================")
        print("--- SCRAPING RESULTS ---")
        print("===========================================")
        print(f"Title: {title}")
        print(f"Raga: {raga}")
        print(f"Tala: {tala}")
        print(f"Composer: {composer}")
        print("\n--- Lyrics ---\n")
        if not lyrics:
            print("ERROR: Lyrics parsing resulted in empty content.")
        else:
            print(lyrics)
        print("\n--- END OF RESULTS ---\n")

    except Exception as e:
        print(f"An unexpected error occurred: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python final_scraper.py <URL>")
        sys.exit(1)
    
    scrape_url = sys.argv[1]
    final_scrape(scrape_url)
