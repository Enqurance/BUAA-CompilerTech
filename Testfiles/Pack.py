import zipfile, os


def get_zip(base_dir, zip_name):
    zp = zipfile.ZipFile(zip_name, 'w', zipfile.ZIP_DEFLATED)
    for dir_path, dir_name, file_names in os.walk(base_dir):
        for file_name in file_names:
            if str(file_name).__contains__(".txt"):
                zp.write(os.path.join(dir_path, file_name))
    zp.close()


if __name__ == '__main__':
    zip_name = 'File.zip'
    get_zip('./', zip_name)
