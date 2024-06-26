package static

import (
	"embed"
)

//go:embed apidocs.swagger.json
var SwaggerJson embed.FS
