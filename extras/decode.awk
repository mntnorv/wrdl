BEGIN {
	RS = ""
	FS = ""
	convert = "\nABCDEFGHIJKLMNOPQRSTUVWXYZ"

	# For character to number conversion
	for (n=0; n<256; n++) {
		ord[sprintf("%c", n)] = n
	}

	shift = 0
	lastNum = 0
}

function fiveBitDecode(char) {
	currentNum = ord[char]

	if (shift >= 8) {
		shift = shift - 8
	}

	while (shift < 8) {
		if (shift > 0 && shift < 5) {
			lastNum = or(lastNum, and(lshift(currentNum, 5 - shift), 0x1F))
			printf "%c", substr(convert, lastNum + 1, 1)
		}

		lastNum = and(rshift(currentNum, shift), 0x1F)
		if (shift <= 3) {
			printf "%c", substr(convert, lastNum + 1, 1)
			lastNum = 0
		}

		shift = shift + 5
	}
}

{
	for (i = 1; i <= NF; i++) {
		fiveBitDecode($i)
	}
}