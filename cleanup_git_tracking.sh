#!/bin/bash

# Script untuk remove file-file yang sudah ter-track tapi seharusnya di-ignore

echo "=== Cleaning up Git tracking ==="
echo ""

# Remove heap dumps
echo "Removing heap dumps..."
git rm --cached java_pid*.hprof 2>/dev/null || echo "No heap dumps found"

# Remove screenshots
echo "Removing screenshots..."
git rm --cached screenshot*.png 2>/dev/null || echo "No screenshots found"
git rm --cached home_after_nav.png 2>/dev/null || echo "No home_after_nav.png found"

# Remove build files (jika ada yang ter-track)
echo "Removing build files..."
git rm -r --cached app/build/ 2>/dev/null || echo "No build files found"

# Remove .gradle cache (jika ada yang ter-track)
echo "Removing .gradle cache..."
git rm -r --cached .gradle/ 2>/dev/null || echo "No .gradle cache found"

echo ""
echo "✅ Cleanup complete!"
echo ""
echo "📝 Next steps:"
echo "1. Review changes: git status"
echo "2. Commit: git commit -m 'Remove temporary files from Git tracking'"
echo "3. Push: git push"

