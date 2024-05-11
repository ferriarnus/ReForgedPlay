#!/usr/bin/env python3
import os

# Whether to add entries present in the original but not yet in the translation
# Can be very useful to complete translations but makes it difficult to tell which entries have yet to be translated.
# May be set to True, False or a string which will be prepended to yet-to-be-translated lines
include_missing: str | bool = False

# Whether to keep entries which are identical between translation and original
# Disabling can be useful to clean up partially translated files, but it will also remove entries where the translation
# just happens to match, so keep those by default.
include_originals = True

template_file_name = 'en_US.lang'

with open(template_file_name) as f:
    template_lines = f.readlines()

for file_name in os.listdir('.'):
    if not file_name.endswith('.lang'):
        continue

    with open(file_name, 'r') as f:
        lines = f.readlines()

    entries = {}
    for line_nr, line in enumerate(lines):
        if line.startswith('#') or line == '\n':
            continue
        if '=' not in line:
            print('Missing `=` in `{}` at line {}: {}'.format(file_name, line_nr + 1, line))
            continue
        key, value = line.split('=', 1)
        entries[key] = value

    lines = []
    header = []
    header_complete = False
    for line in template_lines:
        if line.startswith('#') or line == '\n':
            if header_complete:
                header.clear()
                header_complete = False
            header.append(line)
        else:
            key, default_value = line.split('=', 1)
            value = entries[key] if key in entries else default_value
            has_translation = key in entries and (value != default_value or include_originals)

            if not has_translation and include_missing is False:
                header_complete = True
                continue

            if len(header) > 0:
                lines.extend(header)
                header.clear()

            if has_translation or include_missing is True:
                lines.append(key + '=' + value)
            else:
                lines.append(include_missing + key + '=' + value)

    with open(file_name, 'w') as f:
        f.writelines(lines)
