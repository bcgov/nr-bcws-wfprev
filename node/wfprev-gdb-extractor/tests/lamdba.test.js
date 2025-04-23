const { handler } = require('../lambda');
const awsServerlessExpress = require('aws-serverless-express');

// Mock the Express server and response
jest.mock('aws-serverless-express', () => {
  const originalModule = jest.requireActual('aws-serverless-express');
  return {
    ...originalModule,
    proxy: jest.fn()
  };
});

describe('Lambda Handler', () => {
  it('should call awsServerlessExpress.proxy with the correct event and context', () => {
    const fakeEvent = {
      requestContext: {
        http: {
          method: 'POST'
        }
      },
      rawPath: '/upload',
      headers: {
        'content-type': 'application/zip'
      },
      body: 'fakeBodyBase64',
      isBase64Encoded: true
    };

    const fakeContext = {
      functionName: 'testFunction'
    };

    handler(fakeEvent, fakeContext);

    // Assert transformed properties were set
    expect(fakeEvent.httpMethod).toBe('POST');
    expect(fakeEvent.path).toBe('/upload');

    // Assert proxy was called
    expect(awsServerlessExpress.proxy).toHaveBeenCalled();
    expect(awsServerlessExpress.proxy).toHaveBeenCalledWith(
      expect.anything(), // server instance
      expect.objectContaining({
        httpMethod: 'POST',
        path: '/upload'
      }),
      fakeContext
    );
  });
});
