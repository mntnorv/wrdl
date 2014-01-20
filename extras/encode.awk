BEGIN {
	FS = ""
	convert = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

	shift = 0
	lastChar = 0
}

function fiveBitPrint(char) {
	lastChar = or(lastChar, and(lshift(char, shift), 0xFF));

	if (shift >= 3) {
		printf "%c", lastChar
		lastChar = rshift(char, 8 - shift)
	}

	shift = shift + 5
	if (shift >= 8) {
		shift = shift - 8
	}
}

{
	for (i = 1; i <= NF; i++) {
		num = and(index(convert, $i), 0x1F)
		fiveBitPrint(num)
	}

	fiveBitPrint(0)
}

END {
	printf "%c", lastChar
}