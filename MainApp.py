import logging
from logging.config import fileConfig
from PySimpleGUI import Window, Text, Input, FilesBrowse, OK, Cancel
from service.db_utility import DatabaseUtility
from service.file_processor import FileProcessor


class PrimaryUI(Window):

    IMPORT = 'Import'

    EXPORT = 'Export'

    def __init__(self):

        super().__init__('DocDb')

        self.logger = logging.getLogger()

        self.logger.info('Starting MainApp')

        self.__build_ui()

    def __build_ui(self):

        layout = [ [ Text('Filename') ],
                            [ Input(), FilesBrowse()], 
                            [ OK(key=self.IMPORT), Cancel()]]

        self.Layout(layout)

if __name__ == '__main__':

    fileConfig('logging_config.ini')

    primary_ui = PrimaryUI()

    db_util = DatabaseUtility()

    file_processor = FileProcessor()

    while True:

        event, file_names  = primary_ui.Read()

        if event is not None:

            # Process the data 

            if file_names[0] != '':

                if event == primary_ui.IMPORT: db_util.save_docs(file_processor.convert_file_names_to_name_data_dict(file_names[0]))

                # elif event == primary_ui.EXPORT: print('Export')

        else: break