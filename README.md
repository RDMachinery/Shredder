# Shredder

A small, dependency-free command-line utility written in Java that securely overwrites files with zeroes before deleting them. It can process a single file or recursively shred an entire directory tree.

## Features

- Overwrites file contents with zero bytes before deletion
- Works on individual files or whole directories (recursive)
- Removes directories once their contents have been shredded
- Writes in 8 KB chunks, so large files don't need to be loaded into memory
- Uses synchronous writes (`rws` mode) and an explicit `sync()` to push data to disk before the file is deleted
- Skips files that aren't writable and reports the problem instead of crashing
- No external dependencies, just the Java standard library

## Requirements

- Java Development Kit (JDK) 8 or newer

Check your installation with:

```bash
java -version
javac -version
```

## Build

```bash
javac Shredder.java
```

## Usage

```bash
java Shredder <path-to-file-or-directory>
```

### Examples

Shred a single file:

```bash
java Shredder secret.txt
```

Shred a whole directory and everything inside it:

```bash
java Shredder old-project/
```

### Example output

```
Shredded: /home/user/secret.txt (2048 bytes zeroed)
Shredding operation completed successfully.
```

## How it works

1. The target path is checked to make sure it exists.
2. If it is a directory, the program recurses into every child, then deletes the empty directory.
3. For each file, it opens the file in `rws` mode and writes zeroes over its entire length in 8 KB chunks.
4. It calls `FileDescriptor.sync()` to force the data onto the physical disk.
5. Finally, the zeroed file is deleted.

## Limitations

Please read these before relying on this tool for sensitive data:

- **Single pass only.** The file is overwritten once with zeroes. This is not a multi-pass scheme such as DoD 5220.22-M or Gutmann.
- **SSDs and flash storage.** Wear levelling and over-provisioning mean the drive may keep old copies of data in blocks that overwrites never touch. Zeroing a file on an SSD does not guarantee the original data is gone. Full-disk encryption or the drive's secure-erase / TRIM features are more reliable there.
- **Journaling, copy-on-write, and snapshotting filesystems** (e.g. ext4 with data journaling, Btrfs, ZFS, APFS, NTFS with shadow copies) may retain earlier versions of the data elsewhere on disk.
- **Backups, cloud sync, and temp files** are not touched. Copies in the recycle bin, cloud storage, or application caches are not affected.
- **File metadata** (such as the file name) may still be recoverable from the filesystem even after the contents are overwritten.
- **Symbolic links** are not specially handled; use caution when pointing the tool at directories that contain them.
- Files that are read-only or otherwise not writable are skipped.

## Warning

**This tool permanently deletes data. There is no undo and no confirmation prompt.** Double-check the path you pass in before running it. Use at your own risk.

## License

Released under the MIT License.
