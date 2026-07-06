package server

import (
	"regexp"
	"strings"
)

var (
	phoneRegexp = regexp.MustCompile(`^\+[1-9]\d{7,14}$`)
	emailRegexp = regexp.MustCompile(`^[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$`)
)

func isValidPhone(value string) bool {
	value = strings.TrimSpace(value)
	return phoneRegexp.MatchString(value)
}

func isValidEmail(value string) bool {
	value = strings.TrimSpace(value)
	return emailRegexp.MatchString(value)
}