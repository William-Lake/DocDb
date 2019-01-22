import os
import logging

class FileProcessor(object):

    def __init__(self):

        self.logger = logging.getLogger()

        self.logger.info('FileProcessor Loaded')

    def convert_file_names_to_name_data_dict(self,file_names):

        name_data_dict = {}

        for file_name in file_names.split(';'): name_data_dict[os.path.basename(file_name)] = open(file_name,'rb').read()

        return name_data_dict
            