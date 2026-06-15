import React from 'react';
import ReactDiffViewer from 'react-diff-viewer-continued';

interface Props {
  oldValue: string;
  newValue: string;
}

const CoverLetterDiffViewer: React.FC<Props> = ({ oldValue, newValue }) => {
  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl shadow border border-gray-100 dark:border-gray-700 p-6 overflow-x-auto text-sm">
      <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-4">Version Comparison</h3>
      <div className="border border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden">
        <ReactDiffViewer
          oldValue={oldValue}
          newValue={newValue}
          splitView={true}
          useDarkTheme={document.documentElement.classList.contains('dark')}
          styles={{
            variables: {
              light: {
                diffViewerBackground: '#fff',
                diffViewerColor: '#333',
                addedBackground: '#e6ffed',
                addedColor: '#24292e',
                removedBackground: '#ffeef0',
                removedColor: '#24292e',
                wordAddedBackground: '#acf2bd',
                wordRemovedBackground: '#fdb8c0',
                emptyLineBackground: '#fafbfc'
              },
              dark: {
                diffViewerBackground: '#1e1e1e',
                diffViewerColor: '#d4d4d4',
                addedBackground: '#044B53',
                addedColor: '#d4d4d4',
                removedBackground: '#632F34',
                removedColor: '#d4d4d4',
                wordAddedBackground: '#055d67',
                wordRemovedBackground: '#7d3840',
                emptyLineBackground: '#1e1e1e'
              }
            }
          }}
        />
      </div>
    </div>
  );
};

export default CoverLetterDiffViewer;
