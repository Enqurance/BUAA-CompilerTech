import zipfile
import os


def do_zip_compress(dirpath):
    output_name = "File.zip"
    parent_name = os.path.dirname(dirpath)
    zip = zipfile.ZipFile(output_name, "w", zipfile.ZIP_DEFLATED)
    # 多层级压缩
    for root, dirs, files in os.walk(dirpath):
        for file in files:
            if str(file).startswith("~$"):
                continue
            filepath = os.path.join(root, file)
            writepath = os.path.relpath(filepath, parent_name)
            zip.write(filepath, writepath)
    zip.close()


if __name__ == "__main__":
    dirpath = r"./src"
    do_zip_compress(dirpath)
