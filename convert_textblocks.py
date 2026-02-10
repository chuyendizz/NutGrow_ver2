import re

# Read the file
with open(r'c:\test\Nutgrow\src\main\java\com\demo\nutgrow\service\AIService.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace all text blocks with regular strings
# Pattern: String.format("""...""", args)
def replace_text_block(match):
    indent = match.group(1)
    inner_text = match.group(2)
    args = match.group(3)
    
    # Split by lines and convert to concatenated string
    lines = inner_text.split('\n')
    result_lines = []
    for line in lines:
        # Remove leading/trailing whitespace from each line
        line = line.strip()
        if line:
            # Escape quotes
            line = line.replace('"', '\\"')
            result_lines.append(f'"{line}\\n" +')
    
    # Remove last ' +' and construct final string
    if result_lines:
        result_lines[-1] = result_lines[-1][:-3]  # Remove last ' +'
    
    result = indent + 'String prompt = ' + '\n' + indent + '        '.join(result_lines)
    if args:
        result += ' + ' + args
    result += ';'
    
    return result

# This is complex, let's do manual replacement
print("File has text blocks that need manual conversion")
print("Please run: mvn clean compile -DskipTests")
