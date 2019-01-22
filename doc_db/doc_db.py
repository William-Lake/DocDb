import logging
from logging.config import fileConfig
from PySimpleGUI import Window, Text, Input, FilesBrowse, OK, Cancel, Frame, Combo, FolderBrowse
from db_utility import DatabaseUtility
from file_processor import FileProcessor


class PrimaryUI(Window):

    IMPORT = 'Import'

    EXPORT = 'Export'

    def __init__(self):

        super().__init__('DocDb')

        self.logger = logging.getLogger()

        self.logger.info('Starting MainApp')

        self.__build_ui()

    def __build_ui(self):

        layout = [ [ self.__build_import_ui() ], [ self.__build_export_ui() ] ]

        self.Layout(layout)

    def __build_import_ui(self):

        return Frame(title='Import',layout=[ [ Text('Filename') ],
                            [ Input(), FilesBrowse()], 
                            [ OK(key=self.IMPORT)]])

    def __build_export_ui(self):

        self.cmb_doc_names = Combo([])

        return Frame(title='Export',layout=[ [ self.cmb_doc_names, FolderBrowse() ],
                            [ OK(key=self.EXPORT)]])

    def set_doc_names(self,doc_names):

        self.cmb_doc_names.Values = doc_names

if __name__ == '__main__':

    fileConfig('logging_config.ini')

    db_util = DatabaseUtility()

    primary_ui = PrimaryUI()

    primary_ui.set_doc_names(db_util.get_saved_doc_names())

    file_processor = FileProcessor()

    while True:

        event, file_names  = primary_ui.Read()

        if event is not None:

            # Process the data 

            if file_names[0] != '':

                if event == primary_ui.IMPORT: db_util.save_docs(file_processor.convert_file_names_to_name_data_dict(file_names[0]))

                # elif event == primary_ui.EXPORT: 

        else: break